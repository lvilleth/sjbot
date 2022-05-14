package org.sjb.clients;

import lombok.Getter;
import org.sjb.clients.twitch.TwitchClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClientManager {

    @Getter
    private final TwitchClient twitchClient;

    @Inject
    public ClientManager(TwitchClient twitchClient){
        this.twitchClient = twitchClient;
    }

}
