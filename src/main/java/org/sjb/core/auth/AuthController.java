package org.sjb.core.auth;

import org.sjb.clients.ClientManager;
import org.sjb.clients.twitch.TwitchClient;
import org.sjb.clients.twitch.models.dto.OAuthSuccess;
import org.sjb.core.exceptions.ClientAuthorizationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Session;
import spark.Spark;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;

import static java.util.Objects.isNull;
import static org.sjb.core.utils.Constants.CLIENT_ID;
import static org.sjb.core.utils.Constants.STATE;
import static spark.Spark.get;

@Singleton
public class AuthController {

    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final Random random;
    private final ClientManager clientManager;

    @Inject
    public AuthController(ClientManager clientManager, Random random){
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
                    "<a href=\"https://id.twitch.tv/oauth2/authorize?response_type=code&client_id="+clientId+"&redirect_uri=http://localhost:"+Spark.port()+"/auth/twitch/callback&scope=chat:read chat:edit channel:moderate moderator:manage:banned_users moderation:read" +
                    "&state="+state+"\">Click here to Authorize</a>" +
                    "</body></html>";
        });

        get("/auth/twitch/callback", ((req, resp) -> {
            String reqState = req.queryParams(STATE);
            String clientState = req.session().attribute(STATE);

            boolean noState = isNull(reqState) || reqState.isBlank() || isNull(clientState) || clientState.isBlank();
            if(noState || !reqState.equals(clientState)){
                throw new ClientAuthorizationError();
            }

            String redirectUri = String.format("http://localhost:%d/auth/twitch/callback", Spark.port());
            TwitchClient twitchClient = clientManager.getTwitchClient();
            OAuthSuccess authSuccess = twitchClient.getService()
                    .oAuthCodeFlow(req.queryParams("code"), redirectUri)
                    .orElseThrow(ClientAuthorizationError::new);
            twitchClient.connect(authSuccess.getLogin(), authSuccess.getAccessToken());
            return "Success! You can close this window";
        }));
    }

}
