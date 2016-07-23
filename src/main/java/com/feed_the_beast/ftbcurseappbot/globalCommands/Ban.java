package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.PersistanceEventType;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class Ban extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        boolean canBan = false;
        if (msg.senderPermissions.contains(GroupPermissions.BAN_USER)) {
            canBan = true;
        }

        String message = msg.body;
        String[] msplit = msg.body.split(" ");
        if (msplit.length > 1)

        {
            if (canBan) {
                String desc = "";
                if (msplit.length > 2) {
                    desc = msplit[2];
                }
                webSocket.sendMessage(msg.conversationID, "You can ban " + msplit[1] + "!");
                MongoConnection.logEvent(PersistanceEventType.BAN, msg.serverID, msg.conversationID, msg.senderID, 9999, desc);//TODO put the userID of the person getting banned here!
            } else {
                webSocket.sendMessage(msg.conversationID, "You do not have permission to use the ban command!");
            }
        } else

        {
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
