package org.sjb.clients.twitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.sjb.clients.twitch.config.TwitchConfiguration;
import org.sjb.core.Storage;
import org.sjb.core.config.JsonConfigurationProvider;
import org.sjb.core.utils.Constants;
import org.sjb.core.persistence.JpaInitializer;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TwitchModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JpaInitializer.class).asEagerSingleton();
    }

    @Provides
    protected TwitchClient twitchClient(HttpClient httpClient, ObjectMapper mapper, JsonConfigurationProvider provider) throws Exception {
        provider.config();
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
