package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.data.MongoCommand;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Setcmd extends CommandBase {
    @Override public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        if (msg.senderPermissions.contains(GroupPermissions.BAN_USER)) { //for now don't let everyone create commands
            if (MongoConnection.isPersistanceEnabled()) {
                String[] parts = msg.body.split(" ", 3);
                if (parts.length < 3) {
                    Main.sendMessage(msg.conversationID, "usage: " + Config.getBotTrigger() + "setcmd <command> <content>");

                } else {
                    String commandRegex = parts[1];
                    String content = parts[2];
                    boolean botTrigger = false;
                    String regex = commandRegex;
                    if (commandRegex.startsWith(Config.getBotTrigger())) {
                        botTrigger = true;
                        regex = regex.substring(1);
                    }
                    MongoConnection.createOrModifyCommandForServer(regex, content, null, msg.rootConversationID, botTrigger);
                    Optional<List<MongoCommand>> commands = MongoConnection.getCommandsForServer(msg.rootConversationID);
                    //update cache
                    commands.ifPresent(mongoCommands -> Main.getCacheService().setServerCommandsEntry(msg.rootConversationID, mongoCommands));
                }
            } else {
                Main.sendMessage(msg.conversationID, "can not add command as persistence is disabled");

            }
        } else {
            Main.sendMessage(msg.conversationID, "can not add command as you don't have the necessary permissions");
        }
    }

    @Override public Pattern getTriggerRegex () {
        return getSimpleCommand("setcmd");
    }

    @Override public String getHelp () {
        return "setcmd <!name> <text>";
    }
}
