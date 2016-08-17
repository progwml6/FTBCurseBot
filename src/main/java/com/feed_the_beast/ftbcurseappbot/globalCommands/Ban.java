package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.PersistanceEventType;
import com.feed_the_beast.javacurselib.common.classes.GroupMemberContract;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
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
                Optional<GroupMemberContract> member = Main.getCacheService().getServerMember(msg.rootConversationID, msplit[1], true);

                if (member.isPresent()) {
                    webSocket.sendMessage(msg.conversationID, "You can ban " + msplit[1] + "!");
                    MongoConnection.logEvent(PersistanceEventType.BAN, msg.rootConversationID, msg.conversationID, msg.senderID, msg.senderName, member.get().userID, msplit[1], desc, true);
                } else {
                    webSocket.sendMessage(msg.conversationID, "can not find " +msplit[1] + " in this server to ban!");
                }
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
