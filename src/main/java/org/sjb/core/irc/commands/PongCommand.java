package org.sjb.core.irc.commands;

import lombok.ToString;
import org.sjb.core.commands.AbstractCommand;
import org.sjb.core.commands.AsyncCommand;
import org.sjb.core.irc.IRCMessage;

import java.net.http.WebSocket;
import java.util.concurrent.Future;

@ToString(callSuper = true)
public class PongCommand extends AbstractCommand implements AsyncCommand<WebSocket> {

    @ToString.Exclude
    private final WebSocket socket;
    private final IRCMessage ping;

    /**
     * @param ping the received PING message
     * @param socket websocket to relay the response
     */
    public PongCommand(IRCMessage ping, WebSocket socket){
        super("PONG");
        this.socket = socket;
        this.ping = ping;
    }

    @Override
    public Future<WebSocket> execute() {
        String message = "PONG ".concat(ping.getParams().get(0));
        log.debug(message);
        return socket.sendText(message, true);
    }

}
