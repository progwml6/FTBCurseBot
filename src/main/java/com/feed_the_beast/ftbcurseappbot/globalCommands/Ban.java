package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class Ban extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        String lg = "Ban sender: " + msg.senderName + " " + msg.senderID + " " + msg.serverID + " ";
        boolean canBan = false;
        for (int i : msg.senderRoles) {
            lg += i + " ";
            if (i == -1 || i == 1 || i == 2 || i == 512) {
                canBan = true;
            }
        }
        log.info(lg);

        String message = msg.body;
        String[] msplit = msg.body.split(" ");
        if (msplit.length > 1) {
            if (canBan) {
                webSocket.sendMessage(msg.conversationID, "You can ban " + msplit[1] + "!");
            }
        } else {
            webSocket.sendMessage(msg.conversationID, "You need to enter a username to ban!");
        }

    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("ban");
    }

    @Override
    public String getHelp () {
        return "bans <user> <optional reason> permanently bans a user from the server";
    }

    @Override
    public boolean canExecuteInPM () {
        return false;
    }
}
