package org.sjb.core.irc;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

/**
 * https://datatracker.ietf.org/doc/html/rfc1459.html#section-2.3
 *
 * <message>  ::= [':' <prefix> <SPACE> ] <command> <params> <crlf>
 * <prefix>   ::= <servername> | <nick> [ '!' <user> ] [ '@' <host> ]
 * <command>  ::= <letter> { <letter> } | <number> <number> <number>
 * <SPACE>    ::= ' ' { ' ' }
 * <params>   ::= <SPACE> [ ':' <trailing> | <middle> <params> ]
 *
 * <middle>   ::= <Any *non-empty* sequence of octets not including SPACE
 *                or NUL or CR or LF, the first of which may not be ':'>
 * <trailing> ::= <Any, possibly *empty*, sequence of octets not including
 *                  NUL or CR or LF>
 *
 * <crlf>     ::= CR LF
 */
@Builder(access = AccessLevel.PROTECTED)
@ToString @Getter
public class IRCMessage {

    private final String raw;

    private final String nick;
    private final String host;
    private final String rawPrefix;

    private final String command;
    private final String rawCommand;

    private final List<String> params;
    private final String rawParams;

    private final Map<String, String> tags;
    private final String rawTags;

    /**
     * @param message raw message
     * @return parsed IRCMessage
     */
    public static IRCMessage parse(CharSequence message){
        return parse(message.toString());
    }

    /**
     * @param message raw message
     * @return parsed IRCMessage
     */
    public static IRCMessage parse(String message) {
        String host = "";
        String nick = "";
        String rawPrefix = "";
        String command = "";
        String rawCommand = "";
        List<String> params;
        String rawParams = "";
        Map<String, String> tags = new HashMap<>();
        String rawTags = "";

        int idx = 0;
        int endIdx;

        // tags
        if (message.charAt(idx) == '@') {
            endIdx = message.indexOf(' ', 1);
            rawTags = message.substring(idx + 1, endIdx);

            String[] tokens = rawTags.split(";");
            int keyIdx;
            for (String token : tokens) {
                keyIdx = token.indexOf('=');
                if (keyIdx == -1) continue;
                tags.put(token.substring(0, keyIdx), token.substring(keyIdx + 1));
            }

            idx = endIdx + 1;
        }

        // prefix
        if (message.charAt(idx) == ':') { // else is a PING theres no prefix
            idx += 1;
            endIdx = message.indexOf(' ', idx);
            rawPrefix = message.substring(idx, endIdx);

            int endNick = rawPrefix.indexOf('!');
            if (endNick == -1) {
                host = rawPrefix;
            } else {
                nick = rawPrefix.substring(0, endNick);
                host = rawPrefix.substring(endNick + 1);
            }

            idx = endIdx + 1;
        }

        // command
        endIdx = message.length();
        rawCommand = message.substring(idx, endIdx).trim();

        List<String> tokens = new ArrayList<>(Arrays.asList(rawCommand.split("\\s+")));
        command = tokens.remove(0);

        // parameters
        rawParams = String.join(" ", tokens);
        params = tokens;

        return IRCMessage.builder()
                .raw(message)
                .rawTags(rawTags)
                .tags(tags)
                .rawPrefix(rawPrefix)
                .nick(nick)
                .host(host)
                .rawCommand(rawCommand)
                .command(command)
                .rawParams(rawParams)
                .params(params)
                .build();
    }

    public static List<IRCMessage> parseMulti(String message) {
        String[] rawMessages = message.split("\\R");
        List<IRCMessage> messageList = new ArrayList<>(rawMessages.length);

        for (int i = 0; i < rawMessages.length; i++) {
            messageList.add(i, parse(rawMessages[i]));
        }

        return messageList;
    }

    public static List<IRCMessage> parseMulti(CharSequence message){
        return parseMulti(message.toString());
    }
}



