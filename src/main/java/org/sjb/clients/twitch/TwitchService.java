package org.sjb.clients.twitch;

import org.sjb.clients.twitch.models.TwitchEntity;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class TwitchService {

    private final TwitchDao twitchDao;

    @Inject
    public TwitchService(TwitchDao twitchDao) {
        this.twitchDao = twitchDao;
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

}
