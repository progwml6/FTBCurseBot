package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;

import java.util.Optional;
import java.util.regex.Pattern;

public class Curseuuid extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        Optional<String> channelName = Main.getCacheService().getContacts().get().getChannelNamebyId(msg.conversationID);
        Optional<String> serverName = Main.getCacheService().getContacts().get().getGroupNamebyId(msg.rootConversationID);

        if (serverName.isPresent() && channelName.isPresent()) {
            Main.sendMessage(msg.conversationID, "Server: " + serverName.get() + " is " + msg.rootConversationID + " ; Channel: " + channelName.get() + " is " + msg.conversationID);
        } else {
            if (serverName.isPresent()) {
                Main.sendMessage(msg.conversationID, "Server: " + serverName + " is " + msg.rootConversationID + " ; Channel: is " + msg.conversationID);
            }
            channelName.ifPresent(s -> Main.sendMessage(msg.conversationID, "Server is " + msg.rootConversationID + " ; Channel: " + s + " is " + msg.conversationID));
        }
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("curseuuid");
    }

    @Override
    public String getHelp () {
        return "gets server/channel uuid for debugging purposes";
    }
}
