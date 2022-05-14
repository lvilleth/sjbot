package org.sjb.clients.twitch.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class OAuthSuccess {

    private String login;

    private String accessToken;

}
