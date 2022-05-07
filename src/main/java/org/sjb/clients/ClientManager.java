package org.sjb.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.sjb.clients.twitch.TwitchClient;
import org.sjb.clients.twitch.config.TwitchConfiguration;
import org.sjb.core.Storage;
import org.sjb.core.utils.Constants;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClientManager {

    @Getter
    private final TwitchClient twitchClient;

    public ClientManager(HttpClient httpClient, ObjectMapper mapper){
        this.twitchClient = twitchClient(httpClient, mapper);
    }

    private TwitchClient twitchClient(HttpClient httpClient, ObjectMapper mapper) {
        TwitchConfiguration twitchConfiguration;
        Map<String, Object> config = Storage.getInstance().get(Constants.CONFIG_KEY);
        if(Objects.nonNull(config) && config.containsKey(TwitchConfiguration.KEY)){
            Map<String, Object> json = (Map<String, Object>) config.get(TwitchConfiguration.KEY);
            twitchConfiguration = mapper.convertValue(json, TwitchConfiguration.class);
        } else {
            twitchConfiguration = new TwitchConfiguration("", "", new HashMap<>());
        }
        return new TwitchClient(httpClient, twitchConfiguration);
    }

}
