package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;

import java.util.regex.Pattern;

public class DelCurseCheck extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        if (msg.senderPermissions.contains(GroupPermissions.BAN_USER)) { //for now don't let everyone create commands
            if (MongoConnection.isPersistanceEnabled()) {
                String[] parts = msg.body.split(" ", 3);
                if (parts.length < 2) {
                    Main.sendMessage(msg.conversationID, "usage: " + Config.getBotTrigger() + "delcursecheck <author> <type(optional)>");
                } else {
                    String author = parts[1];
                    String type = null;
                    if (parts.length >= 3) {
                        type = parts[2];
                    }
                    MongoConnection.removeCurseforgeCheckFromServer(author, type, msg.rootConversationID, msg.conversationID);
                }
            } else {
                Main.sendMessage(msg.conversationID, "can not delete check as persistence is disabled");

            }
        } else {
            Main.sendMessage(msg.conversationID, "can not delete check as you don't have the necessary permissions");
        }
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("delcursecheck");
    }

    @Override
    public String getHelp () {
        return "delcursecheck <author> <type(optional)>";
    }
}