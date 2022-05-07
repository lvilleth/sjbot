package org.sjb.clients.twitch;

import lombok.RequiredArgsConstructor;
import org.sjb.clients.twitch.commands.TwitchCommandExecutor;
import org.sjb.core.irc.IRCMessage;
import org.sjb.core.irc.commands.PongCommand;

import java.net.http.WebSocket;
import java.util.List;

@RequiredArgsConstructor
public class TwitchMessageHandler {

    private final TwitchCommandExecutor commandExecutor;

    public void handleMessage(CharSequence rawMessage, WebSocket socket){
        List<IRCMessage> parsedMessages = IRCMessage.parseMulti(rawMessage);
        for (IRCMessage msg : parsedMessages) {
            switch (msg.getCommand()) {
                case "PING":
                    /**
                     * Keep alive. Server expects a PONG response.
                     */
                    ping(msg, socket);
                    break;
                case "JOIN":
                    /**
                     * User joined the room. Rooms with over 1000 users do not receive it.
                     * Prototype: :<user>!<user>@<user>.tmi.twitch.tv JOIN #<channel>
                     * Example:
                     * :ronni!ronni@ronni.tmi.twitch.tv JOIN #ronni
                     * :ronni.tmi.twitch.tv 353 ronni = #ronni :ronni
                     * :ronni.tmi.twitch.tv 366 ronni #ronni :End of /NAMES list
                     *
                     */
                    break;
                case "353":
                    /**
                     * List users in the channel.
                     * Example:
                     * :ronni.tmi.twitch.tv 353 ronni = #ronni :ronni
                     */
                    break;
                case "PART":
                    /**
                     * User left the room. Rooms with over 1000 users do not receive it.
                     * Prototype: :<user>!<user>@<user>.tmi.twitch.tv PART #<channel>
                     * Example:
                     * :ronni!ronni@ronni.tmi.twitch.tv PART #dallas
                     */
                default:
                    continue;
            }
        }

    }

    private void ping(IRCMessage msg, WebSocket socket) {
        PongCommand command = new PongCommand(msg, socket);
        commandExecutor.executeCommand(command);
    }

    private void join(IRCMessage msg, WebSocket socket){

    }

    private void part(IRCMessage msg, WebSocket socket){

    }

    private void numeric353(IRCMessage msg, WebSocket socket){

    }

}
