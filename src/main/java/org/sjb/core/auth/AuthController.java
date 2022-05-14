package org.sjb.core.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sjb.clients.ClientManager;
import org.sjb.clients.twitch.TwitchClient;
import org.sjb.clients.twitch.TwitchService;
import org.sjb.core.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Session;
import spark.Spark;

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
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.sjb.core.utils.Constants.*;
import static spark.Spark.get;

@Singleton
public class AuthController {

    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final Random random;
    private final ClientManager clientManager;
    private final TwitchService twitchService;

    @Inject
    public AuthController(HttpClient httpClient, ObjectMapper objectMapper, ClientManager clientManager, TwitchService twitchService, Random random){
        this.httpClient = httpClient;
        this.mapper = objectMapper;
        this.twitchService = twitchService;
        this.random = random;
        this.clientManager = clientManager;
    }

    public void setup(){
        endpoints();
    }

    public void endpoints(){
        log.info(String.format("Authorization TWITCH: http://localhost:%s/auth/twitch", Spark.port()));

        get("/auth/twitch", (req, res) -> {
            String range = "0123456789abcdefghijklmnopqrstuvwxyz";
            String state = random.ints(0, range.length())
                    .limit(16).map(range::charAt)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            String clientId =  clientManager.getTwitchClient().getConfiguration().getClientId();

            Session session = req.session(true);
            session.attribute(CLIENT_ID, clientId);
            session.attribute(STATE, state);

            return "<!DOCTYPE html><html><body>" +
                    "<a href=\"https://id.twitch.tv/oauth2/authorize?response_type=code&client_id="+clientId+"&redirect_uri=http://localhost:"+Spark.port()+"/auth/twitch/callback&scope=chat:read chat:edit moderator:manage:banned_users moderation:read" +
                    "&state="+state+"\">Click here to Authorize</a>" +
                    "</body></html>";
        });

        get("/auth/twitch/callback", ((req, resp) -> {
            String reqState = req.queryParams(STATE);
            String clientState = req.session().attribute(STATE);

            boolean noState = isNull(reqState) || reqState.isBlank() || isNull(clientState) || clientState.isBlank();
            if(noState || !reqState.equals(clientState)){
                resp.status(401);
                return "Error during authentication";
            }

            String clientId = req.session().attribute(CLIENT_ID);
            String clientSecret = clientManager.getTwitchClient().getConfiguration().getClientSecret();

            twitchOauthCodeFlow(req.queryParams("code"), clientId, clientSecret);
            return "Success! You can close this window";
        }));
    }

    private void twitchOauthCodeFlow(String code, String clientId, String clientSecret) throws IOException, InterruptedException {
        Map<String, String> formParams = Map.of(
                CLIENT_ID, clientId,
                CLIENT_SECRET, clientSecret,
                "code", code,
                "grant_type","authorization_code",
                "redirect_uri", String.format("http://localhost:%d/auth/twitch/callback", Spark.port())
        );

        String form = formParams.entrySet().stream()
                .map(e -> String.format("%s=%s", e.getKey(), URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", "NBSChatBot")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create("https://id.twitch.tv/oauth2/token"))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200){
            throw new RuntimeException("Unauthorized");
        }

        HashMap<String,Object> authJson = mapper.readValue(response.body(), HashMap.class);
        log.debug(authJson.toString());

        // get user information
        Map<String, Object> validateJson = twitchValidateToken((String) authJson.get(ACCESS_TOKEN));
        log.debug(validateJson.toString());

        // save to storage
        String storeKey = (String)validateJson.get("login");
        HashMap<String, Object> storeValue = new HashMap<>(Map.of(
                  ACCESS_TOKEN, authJson.get(ACCESS_TOKEN)
                , REFRESH_TOKEN, authJson.get(REFRESH_TOKEN)
                , USER_ID, validateJson.get(USER_ID)
                , SCOPES, validateJson.get(SCOPES)
        ));
        Storage.getInstance().set(storeKey, storeValue);

        // persist refresh token
        String login = (String)validateJson.get("login");
        twitchService.save(login, (String)authJson.get(REFRESH_TOKEN));

        // connect to twitch with the user information
        TwitchClient twitch = clientManager.getTwitchClient();
        twitch.connect(login, (String)authJson.get(ACCESS_TOKEN));
    }

    private HashMap<String, Object> twitchValidateToken(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", "NBSChatBot")
                .header("Authorization", "Bearer ".concat(token))
                .uri(URI.create("https://id.twitch.tv/oauth2/validate"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200){
            throw new RuntimeException("Unauthorized");
        }
        return mapper.readValue(response.body(), HashMap.class);
    }

}
