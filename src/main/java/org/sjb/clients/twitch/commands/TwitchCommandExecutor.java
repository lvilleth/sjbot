package org.sjb.clients.twitch.commands;

import org.sjb.core.commands.AsyncCommand;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TwitchCommandExecutor {

    private final LinkedList<Object> history = new LinkedList<>();

    public synchronized void executeCommand(AsyncCommand<?> command){
        history.addFirst(command);

        try {
            command.execute().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error executing command"+ command);
        }

        // max size for history
        if(history.size() > 10) {
            history.removeLast();
        }
    }

}
