package org.sjb.clients.twitch.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class OAuthError {

    @JsonProperty("status")
    private Integer status;
    @JsonProperty("message")
    private String message;

}
