package org.sjb.clients.twitch;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.sjb.clients.twitch.commands.ChatReplyCommand;
import org.sjb.clients.twitch.commands.TwitchCommandExecutor;
import org.sjb.clients.twitch.config.TwitchConfiguration;
import org.sjb.core.irc.IRCMessage;
import org.sjb.core.irc.commands.PongCommand;

import java.net.http.WebSocket;
import java.util.List;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class TwitchMessageHandler {

    private final TwitchCommandExecutor commandExecutor;
    private final TwitchConfiguration twitchConfig;
    @Setter
    private String botName;

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
                case "PRIVMSG":
                    /**
                     * A message sent in the chat.
                     * Examples:
                     * PRIVMSG #<channel name> :This is a sample message
                     *
                     * @badge-info=;badges=broadcaster/1;client-nonce=459e3142897c7a22b7d275178f2259e0;color=#0000FF;display-name=lovingt3s;emote-only=1;emotes=62835:0-10;first-msg=0;flags=;id=885196de-cb67-427a-baa8-82f9b0fcd05f;mod=0;room-id=713936733;subscriber=0;tmi-sent-ts=1643904084794;turbo=0;user-id=713936733;user-type= :lovingt3s!lovingt3s@lovingt3s.tmi.twitch.tv PRIVMSG #lovingt3s :bleedPurple
                     * To reply to the above chat message, your bot sends the following PRIVMSG message
                     * and includes the 'reply-parent-msg-id' tag, which identifies the chat message you’re replying to.
                     * Example:
                     * @reply-parent-msg-id=885196de-cb67-427a-baa8-82f9b0fcd05f PRIVMSG #lovingt3s :absolutely!
                     */
                    privmsg(msg, socket);
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

    private void privmsg(IRCMessage msg, WebSocket socket) {
        List<String> params = msg.getParams();
        String sender = params.get(0);
        String message = String.join(" ",params.subList(1, params.size()));
        message = message.replaceFirst(":", "");
        String macro = twitchConfig.getMacros().get(message.trim());
        if(nonNull(macro)){
            ChatReplyCommand command = new ChatReplyCommand(msg, socket, macro, botName);
            commandExecutor.executeCommand(command);
        }
    }

    private void join(IRCMessage msg, WebSocket socket){

    }

    private void part(IRCMessage msg, WebSocket socket){

    }

    private void numeric353(IRCMessage msg, WebSocket socket){

    }

}
