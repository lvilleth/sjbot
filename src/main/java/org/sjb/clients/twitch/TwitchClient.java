package org.sjb.clients.twitch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.sjb.clients.twitch.commands.TwitchCommandExecutor;
import org.sjb.clients.twitch.config.TwitchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

import static java.util.Objects.nonNull;

public class TwitchClient implements WebSocket.Listener {

    private final Logger log = LoggerFactory.getLogger(TwitchClient.class);

    private final Charset charset = StandardCharsets.UTF_8;
    private final HttpClient httpClient;

    @Getter
    private final TwitchConfiguration configuration;
    @Getter @Setter(AccessLevel.PRIVATE)
    private String token;
    @Getter @Setter(AccessLevel.PRIVATE)
    private String login;

    private final TwitchMessageHandler messageHandler;

    /**
     *              WebSocket clients               IRC clients
     * SSL	    wss://irc-ws.chat.twitch.tv:443	irc://irc.chat.twitch.tv:6697
     * Non-SSL	ws://irc-ws.chat.twitch.tv:80	irc://irc.chat.twitch.tv:6667
     */
    @Getter
    private final String twitchWebSocketUrl = "wss://irc-ws.chat.twitch.tv:443";

    public TwitchClient(HttpClient httpClient, TwitchConfiguration twitchConfiguration) {
        this.httpClient = httpClient;
        this.messageHandler = new TwitchMessageHandler(new TwitchCommandExecutor(), twitchConfiguration);
        this.configuration = twitchConfiguration;
    }

    public void connect(String login, String token){
        setLogin(login);
        setToken(token);

        httpClient.newWebSocketBuilder()
                .buildAsync(getServerUri(), this)
                .thenApply(socket -> {
                    joinChannel(login, socket);
                    return socket;
                })
                .join();
    }

    public void setLogin(String login) {
        this.login = login;
        messageHandler.setBotName(login);
    }

    public URI getServerUri(){
        return URI.create(twitchWebSocketUrl);
    }

    private ByteBuffer toBuffer(String s){
        return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
    }

    private String fromBuffer(ByteBuffer bb){
        return charset.decode(bb).toString();
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        log.debug(String.format("connected to %s", twitchWebSocketUrl));

        webSocket.sendText("CAP REQ :twitch.tv/membership twitch.tv/tags twitch.tv/commands", true);
        webSocket.sendText("PASS oauth:".concat(token), true);
        webSocket.sendText("NICK ".concat(login), true);

        WebSocket.Listener.super.onOpen(webSocket);
    }

    private void joinChannel(String channel, WebSocket socket){
        socket.sendText("JOIN #".concat(channel), true);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        log.debug(String.format("%s", data));
        messageHandler.handleMessage(data, webSocket);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.debug(String.format("Disconnected from %s status:%d reason:%s", twitchWebSocketUrl, statusCode, reason));
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        log.debug(String.format("Received PING\n%s", fromBuffer(message)));
        webSocket.sendPong(message);
        return WebSocket.Listener.super.onPing(webSocket, message);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        log.debug(String.format("Received PONG\n%s", fromBuffer(message)));
        return WebSocket.Listener.super.onPong(webSocket, message);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error(error.getMessage(), error);
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        log.debug(String.format("Received BINARY\nbytes:%s last:%b", nonNull(data) ? data.remaining() : null, last));
        return WebSocket.Listener.super.onBinary(webSocket, data, last);
    }

}
