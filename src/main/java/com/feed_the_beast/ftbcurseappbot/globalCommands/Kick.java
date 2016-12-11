package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.PersistanceEventType;
import com.feed_the_beast.javacurselib.common.classes.GroupMemberContract;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;
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

        String[] msplit = msg.body.split(" ");
        if (msplit.length > 1) {
            if (canKick) {
                String desc = "";
                if (msplit.length > 2) {
                    desc = msplit[2];
                }
                Optional<GroupMemberContract> member = Main.getCacheService().getServerMember(msg.rootConversationID, msplit[1], true);
                if (member.isPresent()) {
                    webSocket.sendMessage(msg.conversationID, "You can kick " + msplit[1] + "!");
                    MongoConnection.logEvent(PersistanceEventType.KICK, msg.rootConversationID, msg.conversationID, msg.senderID, msg.senderName, member.get().userID, msplit[1], desc, true,
                            new Date().getTime());
                } else {
                    webSocket.sendMessage(msg.conversationID, "can not find " + msplit[1] + " in this server to kick!");
                }

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
