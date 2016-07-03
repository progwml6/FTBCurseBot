package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistance.MongoConnection;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by progwml6 on 7/2/16.
 */
public class Setcmd extends CommandBase {
    @Override public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        if (msg.senderPermissions.contains(GroupPermissions.BAN_USER)) { //for now don't let everyone create commands
            if (MongoConnection.isPersistanceEnabled()) {
                String[] parts = msg.body.split(" ");
                if (parts.length < 3) {
                    webSocket.sendMessage(msg.conversationID, "usage: " + Main.getBotTrigger() + "setcmd <command> <content>" );

                } else {
                    String commandName = parts[1];
                    String content = parts[2];
                    String regex = commandName;//TODO make this autobuild regex if r/ isn't in the string start
                    MongoConnection.createOrModifyCommandForServer(regex, content, null, msg.serverID);
                }
            } else {
                webSocket.sendMessage(msg.conversationID, "can not add command as persistance is disabled");

            }
        } else {
            webSocket.sendMessage(msg.conversationID, "can not add command as you don't have the necessary permissions");
        }
    }

    @Override public Pattern getTriggerRegex () {
        return getSimpleCommand("setcmd");
    }

    @Override public String getHelp () {
        return null;
    }
}
