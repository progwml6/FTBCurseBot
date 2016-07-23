package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.data.MongoCommand;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CustomCommands extends CommandBase {
    @Override public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        Optional<List<MongoCommand>> commands = Main.getCacheService().getCustomCommandsForServer(msg.rootConversationID);
        String ret;
        if (!commands.isPresent()) {//check mongo if nothing exists for the server
            commands = MongoConnection.getCommandsForServer(msg.rootConversationID);
            if (commands.isPresent()) {
                Main.getCacheService().setServerCommandsEntry(msg.rootConversationID, commands.get());
            }
        }
        StringBuilder bdr = new StringBuilder();
        bdr.append("Custom Commands for this server:");
        if (commands.isPresent()) {
            for (MongoCommand c : commands.get()) {
                if (c.isUsesTrigger()) {
                    bdr.append(Main.getBotTrigger()).append(c.getRegex()).append(", ");
                } else {
                    bdr.append(c.getRegex()).append(", ");
                }
            }
            ret = bdr.toString();
        } else {
            ret = "no custom commands exist for this server!";
        }
        if (ret.endsWith(", ")) {
            ret = ret.substring(0, ret.length() - 2);
        }

        webSocket.sendMessage(msg.conversationID, ret);
    }

    @Override public Pattern getTriggerRegex () {
        return getSimpleCommand("customcommands");
    }

    @Override public String getHelp () {
        return "lists custom commands on the server";
    }
}
