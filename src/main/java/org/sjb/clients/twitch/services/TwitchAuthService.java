package org.sjb.clients.twitch.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sjb.clients.twitch.models.dto.OAuthResponse;
import org.sjb.clients.twitch.models.dto.OAuthUserInfo;
import org.sjb.core.exceptions.ClientAuthorizationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.sjb.core.utils.Constants.*;

@Singleton
public class TwitchAuthService {

    private final Logger log = LoggerFactory.getLogger(TwitchAuthService.class);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    @Inject
    public TwitchAuthService(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    public OAuthResponse refreshToken(String refreshToken, String clientId, String clientSecret) throws IOException, InterruptedException {
        Map<String, String> formParams = Map.of(
                CLIENT_ID, clientId,
                CLIENT_SECRET, clientSecret,
                "grant_type", REFRESH_TOKEN,
                REFRESH_TOKEN, refreshToken
        );

        HttpResponse<String> response = sendToOauthTokenEndpoint(formParams);
        if(response.statusCode() != 200){
            throw new ClientAuthorizationError();
        }

        HashMap<String,Object> authJson = mapper.readValue(response.body(), HashMap.class);
        log.debug("token refreshed: " + authJson.toString());
        return mapper.convertValue(authJson, OAuthResponse.class);
    }

    public OAuthUserInfo userInfo(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", "NBSChatBot")
                .header("Authorization", "Bearer ".concat(token))
                .uri(URI.create("https://id.twitch.tv/oauth2/validate"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200){
            throw new ClientAuthorizationError();
        }
        return mapper.readValue(response.body(), OAuthUserInfo.class);
    }

    public OAuthResponse oAuthCodeFlow(String code, String clientId, String clientSecret, String redirectUri) throws IOException, InterruptedException {
        Map<String, String> formParams = Map.of(
                CLIENT_ID, clientId,
                CLIENT_SECRET, clientSecret,
                "code", code,
                "grant_type","authorization_code",
                "redirect_uri", redirectUri
        );

        HttpResponse<String> response = sendToOauthTokenEndpoint(formParams);
        if(response.statusCode() != 200){
            throw new ClientAuthorizationError();
        }

        HashMap<String,Object> authJson = mapper.readValue(response.body(), HashMap.class);
        log.debug(authJson.toString());

        return mapper.convertValue(authJson, OAuthResponse.class);
    }

    private HttpResponse<String> sendToOauthTokenEndpoint(Map<String, String> formParams) throws IOException, InterruptedException {
        String form = formParams.entrySet().stream()
                .map(e -> String.format("%s=%s", e.getKey(), URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", "NBSChatBot")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create("https://id.twitch.tv/oauth2/token"))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
