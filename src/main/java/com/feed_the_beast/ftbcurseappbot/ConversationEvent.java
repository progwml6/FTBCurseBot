package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.ftbcurseappbot.api.ICommandBase;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.PersistanceEventType;
import com.feed_the_beast.javacurselib.common.classes.GroupMemberContract;
import com.feed_the_beast.javacurselib.common.enums.ConversationNotificationType;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.service.contacts.contacts.GroupNotification;
import com.feed_the_beast.javacurselib.service.contacts.contacts.GroupRoleNotification;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.handler.tasks.Task;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

@Slf4j
public class ConversationEvent implements Task<ConversationMessageNotification> {
    @Override
    public void execute (@Nonnull WebSocket webSocket, @Nonnull ConversationMessageNotification msg) {
        if (Config.isDebugEnabled()) {
            log.debug(new Date(msg.timestamp).toString() + " " + msg.body + " " + new Date().toString());
        }
        if (msg.notificationType == ConversationNotificationType.NORMAL || msg.notificationType == ConversationNotificationType.EDITED) {
            Optional<ICommandBase> command = CommandRegistry.getCommand(msg.rootConversationID, msg.body);
            if (command.isPresent() && Main.getSession().isPresent() && msg.senderID != Main.getSession().get().user.userID) {//bot cannot execute commands for security reasons
                command.get().onMessage(webSocket, msg);
            } else {//custom server commands
                if (Main.getSession().isPresent() && msg.senderID != Main.getSession().get().user.userID) {//bot can't execute commands
                    CommandRegistry.processServerCommands(msg.rootConversationID, webSocket, msg);
                }
            }
            if (msg.notificationType == ConversationNotificationType.EDITED) {
                if (MongoConnection.isPersistanceEnabled()) {
                    MongoConnection
                            .logEvent(PersistanceEventType.getTypeFromConversationNotificationType(msg.notificationType), msg.rootConversationID, msg.conversationID, msg.editedUserID,
                                    msg.editedUsername, msg.senderID,
                                    msg.senderName, msg.body, msg.senderName == Config.getUsername(), msg.editedTimestamp, msg.timestamp);
                }
            } else if (msg.notificationType == ConversationNotificationType.NORMAL) {
                if (MongoConnection.isPersistanceEnabled()) {
                    MongoConnection
                            .logEvent(PersistanceEventType.getTypeFromConversationNotificationType(msg.notificationType), msg.rootConversationID, msg.conversationID, -1,
                                    null, msg.senderID,
                                    msg.senderName, msg.body, msg.senderName == Config.getUsername(), msg.timestamp, msg.timestamp);
                }
            }
            if (msg.mentions != null && msg.mentions.length > 0) {
                Optional<GroupNotification> gn = Main.getCacheService().getGroupNotification(msg.rootConversationID);
                String msgsend = "";
                if (gn.isPresent()) {
                    for (int i : msg.mentions) {
                        Optional<GroupMemberContract> member = Main.getCacheService().getServerMember(msg.rootConversationID, i, true);
                        boolean canView = false;
                        if (member.isPresent()) {
                            for (int j : member.get().roles) {
                                for (GroupRoleNotification role : gn.get().roles) {
                                    if (role.roleID == j) {
                                        Set<GroupPermissions> perms = gn.get().rolePermissions.get(msg.conversationID);
                                        if (perms != null && perms.contains(GroupPermissions.ACCESS)) {
                                            canView = true;
                                        }
                                    }
                                }
                            }
                            if (!canView) {
                                if (msgsend.length() == 0) {
                                    msgsend += "User(s) can't see message: ";
                                }
                                msgsend += member.get().nickName + " ";
                            }
                        } else {
                            //TODO implement
                        }
                    }
                    if (msgsend.length() > 0) {
                        webSocket.sendMessage(msg.conversationID, msgsend);
                    }
                } else {
                    //TODO implement
                }
            }

            webSocket.sendMarkRead(msg.conversationID);
        } else if (msg.notificationType == ConversationNotificationType.DELETED) {
            if (MongoConnection.isPersistanceEnabled()) {
                MongoConnection
                        .logEvent(PersistanceEventType.getTypeFromConversationNotificationType(msg.notificationType), msg.rootConversationID, msg.conversationID, msg.deletedUserID,
                                msg.deletedUsername, msg.senderID,
                                msg.senderName, msg.body, msg.senderName == Config.getUsername(), msg.deletedTimestamp, msg.timestamp);
            }
        }
    }

}
