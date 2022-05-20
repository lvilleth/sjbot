package org.sjb.clients.twitch.commands;

import lombok.ToString;
import org.sjb.core.commands.AbstractCommand;
import org.sjb.core.commands.AsyncCommand;
import org.sjb.core.irc.IRCMessage;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.lang.String.format;

public class BanUserCommand extends AbstractCommand implements AsyncCommand<WebSocket> {

    @ToString.Exclude
    private final IRCMessage ircMsg;
    private final WebSocket socket;
    private final String reason;

    public BanUserCommand(IRCMessage ircMsg, WebSocket socket, String reason) {
        super("BAN");
        this.ircMsg = ircMsg;
        this.socket = socket;
        this.reason = reason;
    }

    @Override
    public Future<WebSocket> execute() {
        try {
            // PRIVMSG #<channel> :/ban <user> <reason>
            String channel = ircMsg.getParams().get(0);
            String user = ircMsg.getNick();
            String reply = format("PRIVMSG %1$s :/ban %2$s %3$s", channel, user, reason);
            return socket.sendText(reply, true);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(socket);
    }

}
