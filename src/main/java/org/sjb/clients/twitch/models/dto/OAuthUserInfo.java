package org.sjb.clients.twitch.models.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor
public class OAuthUserInfo extends OAuthResponse {


    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("login")
    private String login;

    @JsonProperty("user_id")
    private String userId;

    @JsonAlias("scope")
    @JsonProperty("scopes")
    private List<String> scopes;

    @JsonProperty("expires_in")
    private String expires;

}
