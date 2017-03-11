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

public class DelCmd extends CommandBase {
    @Override public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        if (msg.senderPermissions.contains(GroupPermissions.BAN_USER)) { //for now don't let everyone create commands
            if (MongoConnection.isPersistanceEnabled()) {
                String[] parts = msg.body.split(" ", 2);
                if (parts.length < 2) {
                    Main.sendMessage(msg.conversationID, "usage: " + Config.getBotTrigger() + "delcmd <command>");

                } else {
                    String commandRegex = parts[1];
                    boolean botTrigger = false;
                    String regex = commandRegex;
                    if (commandRegex.startsWith(Config.getBotTrigger())) {
                        botTrigger = true;
                        regex = regex.substring(1);
                    }
                    Optional<List<MongoCommand>> commands = MongoConnection.getCommandsForServer(msg.rootConversationID);
                    if (commands.isPresent()) {
                        for (MongoCommand c : commands.get()) {
                            if (c.getPattern().matcher(regex).matches()) {
                                if (!msg.senderPermissions.containsAll(c.getGroupPermissions())) {
                                    Main.sendMessage(msg.conversationID, "can not remove command as you don't have the necessary permissions");
                                    //TODO log permission violation as this command was set to have higher permissions by admins etc.
                                    return;
                                }
                            }
                        }
                        MongoConnection.removeCommandForServer(regex, msg.rootConversationID, botTrigger);
                        Main.sendMessage(msg.conversationID, commandRegex + " has been removed");
                        commands = MongoConnection.getCommandsForServer(msg.rootConversationID);
                        //update cache
                        commands.ifPresent(mongoCommands -> Main.getCacheService().setServerCommandsEntry(msg.rootConversationID, mongoCommands));
                    } else {
                        Main.sendMessage(msg.conversationID, "command " + regex + " does not exist!");
                    }

                }
            } else {
                Main.sendMessage(msg.conversationID, "can not remove command as persistence is disabled");

            }
        } else {
            Main.sendMessage(msg.conversationID, "can not remove command as you don't have the necessary permissions");
        }
    }

    @Override public Pattern getTriggerRegex () {
        return getSimpleCommand("delcmd");
    }

    @Override public String getHelp () {
        return null;
    }
}
