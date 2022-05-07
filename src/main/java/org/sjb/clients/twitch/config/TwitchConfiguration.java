package org.sjb.clients.twitch.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

@Getter
public class TwitchConfiguration {

    public static final String KEY = "twitch";

    @NonNull @JsonProperty("client_id")
    private final String clientId;
    @NonNull @JsonProperty("client_secret")
    private final String clientSecret;
    @NonNull @JsonProperty("macro")
    private final Map<String,String> macro;

    @JsonCreator
    public TwitchConfiguration(
            @NonNull @JsonProperty("client_id") String clientId,
            @NonNull @JsonProperty("client_secret") String clientSecret,
            @NonNull @JsonProperty("macro") Map<String, String> macro
    ){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.macro = macro;
    }

}
