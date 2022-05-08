package org.sjb.clients.twitch.commands;

import lombok.ToString;
import org.sjb.core.commands.AbstractCommand;
import org.sjb.core.commands.AsyncCommand;
import org.sjb.core.irc.IRCMessage;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.Objects.nonNull;

public class ChatReplyCommand extends AbstractCommand implements AsyncCommand<WebSocket> {

    @ToString.Exclude
    private final IRCMessage ircMsg;
    private final WebSocket socket;
    private final String macro;
    private final String botName;

    public ChatReplyCommand(IRCMessage ircMsg, WebSocket socket, String macro, String botName) {
        this.ircMsg = ircMsg;
        this.socket = socket;
        this.macro = macro;
        this.botName = botName;
        this.name = "PRIVMSG";
    }


    @Override
    public Future<WebSocket> execute() {
        try {
            if(nonNull(macro)){
                String reply;
                if(macro.startsWith("$G")){
                    reply = String.format("PRIVMSG #%1$s :%2$s",
                            botName, macro.replaceFirst("\\$G",""));
                } else {
                    reply = String.format(
                            "@reply-parent-msg-id=%1$s PRIVMSG #%2$s :%3$s",
                            ircMsg.getTags().get("id"), botName, macro
                    );
                }
                return socket.sendText(reply, true);
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }
        return CompletableFuture.completedFuture(socket);
    }

}
