package org.sjb.clients.twitch.services;

import org.sjb.clients.twitch.config.TwitchConfiguration;
import org.sjb.clients.twitch.dao.TwitchBotAccountDao;
import org.sjb.clients.twitch.dao.TwitchDao;
import org.sjb.clients.twitch.models.TwitchBotAccountEntity;
import org.sjb.clients.twitch.models.TwitchEntity;
import org.sjb.clients.twitch.models.dto.OAuthResponse;
import org.sjb.clients.twitch.models.dto.OAuthSuccess;
import org.sjb.clients.twitch.models.dto.OAuthUserInfo;
import org.sjb.clients.twitch.models.dto.external.SuspectedBotInsightResponse;
import org.sjb.core.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.sjb.core.utils.Constants.*;

@Singleton
public class TwitchService {

    private final Logger log = LoggerFactory.getLogger(TwitchService.class);

    private final TwitchDao twitchDao;
    private final TwitchBotAccountDao twitchBotAccountDao;
    private final TwitchAuthService authService;
    private final ExternalApiService extApiService;
    private final TwitchConfiguration twitchConfiguration;

    @Inject
    public TwitchService(TwitchDao twitchDao, TwitchBotAccountDao twitchBotAccountDao, TwitchAuthService authService, ExternalApiService extApiService, TwitchConfiguration twitchConfiguration) {
        this.twitchDao = twitchDao;
        this.twitchBotAccountDao = twitchBotAccountDao;
        this.authService = authService;
        this.extApiService = extApiService;
        this.twitchConfiguration = twitchConfiguration;
        if(isBotAccountListOutdated()) {
            updateBotAccountList();
        }
    }

    private boolean isBotAccountListOutdated() {
        return twitchBotAccountDao.oldestUpdate().map(t ->
                Instant.now(Clock.systemUTC()).isAfter(t.plus(30, ChronoUnit.DAYS))
        ).orElse(true);
    }

    public void updateBotAccountList() {
        try {
           SuspectedBotInsightResponse response = extApiService.getSuspectedBotAccountList();
           List<TwitchBotAccountEntity> entities = response.getBots().stream().map( b ->
                   TwitchBotAccountEntity.builder((String)b.get(0))
           ).collect(Collectors.toList());
           twitchBotAccountDao.save(entities);
           log.debug("Size of suspected bot accounts: "+ entities.size());
        } catch (Exception e){
            log.error("Error updating bot account list", e);
        }
    }

    public List<TwitchEntity> list() {
       return twitchDao.list();
    }

    public Optional<TwitchEntity> get(UUID id) {
        return twitchDao.get(id);
    }

    public void delete(UUID id) {
        twitchDao.delete(id);
    }

    public TwitchEntity save(String login, String refreshToken) {
        TwitchEntity entity = TwitchEntity
                .builder(login)
                .refreshToken(refreshToken)
                .build();

        Optional<TwitchEntity> opt = twitchDao.findByLogin(entity.getLogin());
        if(opt.isPresent()) {
            TwitchEntity current = opt.get();
            current.setRefreshToken(refreshToken);
            return twitchDao.update(current);
        } else {
            return twitchDao.create(entity);
        }
    }

    public Optional<String> refreshToken(String login) {
        String accessToken = null;
        try {
            TwitchEntity entity = twitchDao.findByLogin(login)
                    .orElseThrow(() -> new NoResultException("Not Found"));

            OAuthResponse authJson = authService.refreshToken(entity.getRefreshToken(),
                    twitchConfiguration.getClientId(), twitchConfiguration.getClientSecret());

            OAuthUserInfo userInfo = authService.userInfo(authJson.getAccessToken());

            // save to storage
            HashMap<String, Object> storeValue = new HashMap<>(Map.of(
                    ACCESS_TOKEN, authJson.getAccessToken()
                    , REFRESH_TOKEN, authJson.getRefreshToken()
                    , USER_ID, userInfo.getUserId()
                    , SCOPES, userInfo.getScopes()
            ));
            Storage.getInstance().set(login, storeValue);

            if(!authJson.getRefreshToken().equals(entity.getRefreshToken())) {
                entity.setRefreshToken(authJson.getRefreshToken());
                twitchDao.update(entity);
            }
            accessToken = authJson.getAccessToken();
        } catch (NoResultException ignore){
        } catch (IOException | InterruptedException | RuntimeException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(accessToken);
    }

    public Optional<OAuthSuccess> oAuthCodeFlow(String code, String redirectUri) {
        OAuthSuccess oAuthSuccess = null;
        try {
            OAuthResponse authJson = authService.oAuthCodeFlow(code,
                    twitchConfiguration.getClientId(), twitchConfiguration.getClientSecret(),
                    redirectUri
            );

            OAuthUserInfo userInfo = authService.userInfo(authJson.getAccessToken());

            // save to storage
            HashMap<String, Object> storeValue = new HashMap<>(Map.of(
                    ACCESS_TOKEN, authJson.getAccessToken()
                    , REFRESH_TOKEN, authJson.getRefreshToken()
                    , USER_ID, userInfo.getUserId()
                    , SCOPES, userInfo.getScopes()
            ));
            Storage.getInstance().set(userInfo.getLogin(), storeValue);

            // persist refresh token
            save(userInfo.getLogin(), authJson.getRefreshToken());
            oAuthSuccess = new OAuthSuccess(userInfo.getLogin(), authJson.getAccessToken());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(oAuthSuccess);
    }

    public Set<String> suspectedBotsFrom(Set<String> usernames) {
        return twitchBotAccountDao.listByUsername(usernames)
                .stream().map(TwitchBotAccountEntity::getUsername)
                .collect(Collectors.toSet());
    }

}
