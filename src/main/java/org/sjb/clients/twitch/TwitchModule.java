package org.sjb.clients.twitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.sjb.clients.twitch.config.TwitchConfiguration;
import org.sjb.clients.twitch.services.TwitchService;
import org.sjb.core.Storage;
import org.sjb.core.config.JsonConfigurationProvider;
import org.sjb.core.utils.Constants;
import org.sjb.core.persistence.JpaInitializer;

import javax.inject.Singleton;
import java.io.IOException;
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
    protected TwitchClient twitchClient(HttpClient httpClient, TwitchConfiguration twitchConfiguration, TwitchService twitchService) {
        return new TwitchClient(httpClient, twitchConfiguration, twitchService);
    }

    @Provides @Singleton
    public TwitchConfiguration twitchConfiguration(ObjectMapper mapper, JsonConfigurationProvider provider) throws IOException {
        provider.config();
        TwitchConfiguration twitchConfiguration;
        Map<String, Object> config = Storage.getInstance().get(Constants.CONFIG_KEY);
        if(Objects.nonNull(config) && config.containsKey(TwitchConfiguration.KEY)){
            Map<String, Object> json = (Map<String, Object>) config.get(TwitchConfiguration.KEY);
            twitchConfiguration = mapper.convertValue(json, TwitchConfiguration.class);
        } else {
            twitchConfiguration = new TwitchConfiguration("", "", new HashMap<>(),null, null);
        }
        return twitchConfiguration;
    }

}
