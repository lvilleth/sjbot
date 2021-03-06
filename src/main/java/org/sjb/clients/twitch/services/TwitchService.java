package org.sjb.clients.twitch.services;

import org.sjb.clients.twitch.config.TwitchConfiguration;
import org.sjb.clients.twitch.dao.TwitchDao;
import org.sjb.clients.twitch.models.TwitchEntity;
import org.sjb.clients.twitch.models.dto.OAuthResponse;
import org.sjb.clients.twitch.models.dto.OAuthSuccess;
import org.sjb.clients.twitch.models.dto.OAuthUserInfo;
import org.sjb.core.Storage;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.*;

import static org.sjb.core.utils.Constants.*;

@Singleton
public class TwitchService {

    private final TwitchDao twitchDao;
    private final TwitchAuthService authService;
    private final TwitchConfiguration twitchConfiguration;

    @Inject
    public TwitchService(TwitchDao twitchDao, TwitchAuthService authService, TwitchConfiguration twitchConfiguration) {
        this.twitchDao = twitchDao;
        this.authService = authService;
        this.twitchConfiguration = twitchConfiguration;
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

}
