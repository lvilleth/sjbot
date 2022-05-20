package org.sjb.clients.twitch.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

import static java.util.Objects.nonNull;

@Getter
public class TwitchConfiguration {

    public static final String KEY = "twitch";

    @JsonProperty("login")
    private String login;
    @NonNull @JsonProperty("client_id")
    private final String clientId;
    @NonNull @JsonProperty("client_secret")
    private final String clientSecret;
    @NonNull @JsonProperty("macros")
    private final Map<String,String> macros;
    @NonNull @JsonProperty("ban_suspected_bots")
    private final Boolean banSuspectedBots;

    @JsonCreator
    public TwitchConfiguration(
            @NonNull @JsonProperty("client_id") String clientId,
            @NonNull @JsonProperty("client_secret") String clientSecret,
            @NonNull @JsonProperty("macros") Map<String, String> macros,
            @JsonProperty("login") String login,
            @JsonProperty("ban_suspected_bots") Boolean banSuspectedBots
    ){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.macros = macros;
        this.login = login;
        this.banSuspectedBots = nonNull(banSuspectedBots) ? banSuspectedBots : false;
    }

}
