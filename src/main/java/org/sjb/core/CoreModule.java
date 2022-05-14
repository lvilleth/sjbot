package org.sjb.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.sjb.core.config.AppConfiguration;
import org.sjb.core.utils.FileUtils;

import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Properties;
import java.util.Random;

import static java.util.Objects.nonNull;

public class CoreModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Provides
    public AppConfiguration appConfiguration(){
        Properties props = FileUtils.readProperties();
        String profile = props.getProperty("profile");
        if(nonNull(profile)){
            props.putAll(FileUtils.readProperties(profile));
        }
        return AppConfiguration.build(props);
    }

    @Provides
    public HttpClient httpClient(){
        return  HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Provides
    public Random random() {
        return new SecureRandom();
    }

}
