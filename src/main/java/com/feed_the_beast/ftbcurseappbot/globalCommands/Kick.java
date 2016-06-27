package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.persistance.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistance.PersistanceEventType;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class Kick extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        String lg = "Kick sender from server: " + msg.senderName + " " + msg.senderID + " " + msg.serverID + " ";
        boolean canKick = false;
        if (msg.senderPermissions.contains(GroupPermissions.REMOVE_USER)) {
            canKick = true;
        }
        log.info(lg);

        String message = msg.body;
        String[] msplit = msg.body.split(" ");
        if (msplit.length > 1) {
            if (canKick) {
                String desc = "";
                if(msplit.length > 2){
                    desc = msplit[2];
                }
                webSocket.sendMessage(msg.conversationID, "You can kick " + msplit[1] + "!");
                MongoConnection.logEvent(PersistanceEventType.BAN, msg.serverID, msg.conversationID, msg.senderID, 9999, desc);//TODO put the userID of the person getting banned here!
            } else {
                webSocket.sendMessage(msg.conversationID, "You do not have permission to use the kick command!");
            }
        } else {
            webSocket.sendMessage(msg.conversationID, "You need to enter a username to kick!");
        }

    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("kick");
    }

    @Override
    public String getHelp () {
        return "kick <user> <optional reason> kicks a user from the server";
    }

    @Override
    public boolean canExecuteInPM () {
        return false;
    }
}
