package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.javacurselib.examples.app_v1.CurseApp;
import com.feed_the_beast.javacurselib.service.conversations.conversations.ConversationNotificationType;
import com.feed_the_beast.javacurselib.service.conversations.conversations.ConversationType;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.handler.tasks.Task;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

/**
 * Created by progwml6 on 5/7/16.
 */
@Slf4j
public class ConversationEvent implements Task<ConversationMessageNotification> {
    @Override
    public void execute (@Nonnull WebSocket webSocket, @Nonnull ConversationMessageNotification msg) {
        if (msg.notificationType == ConversationNotificationType.NORMAL || msg.notificationType == ConversationNotificationType.EDITED) {
            if (msg.body.startsWith(Main.getBotTrigger() + "ban")) {
                log.info("LOLBAN " + msg.body.replace(Main.getBotTrigger() + "ban", ""));
                webSocket.sendMessage(msg.conversationID, "not actually going to ban " + msg.body.replace(Main.getBotTrigger() + "ban", ""));
            } else if (msg.body.startsWith(Main.getBotTrigger() + "repeat")) {
                log.info("repeat " + msg.body.replace(Main.getBotTrigger() + "repeat", ""));
                webSocket.sendMessage(msg.conversationID, msg.body.replace(Main.getBotTrigger() + "repeat", ""));
            } else if (msg.body.startsWith(Main.getBotTrigger() + "commands")) {
                log.info("commands ");
                webSocket.sendMessage(msg.conversationID, "please try "+ Main.getBotTrigger() + "help");
            } else if (msg.body.startsWith(Main.getBotTrigger() + "api")) {
                log.info("api " + msg.body.replace(Main.getBotTrigger() + "api", ""));
                webSocket.sendMessage(msg.conversationID, "CurseApp api is located at http://api.feed-the-beast.com/curseapiaccess.php");
            } else if (msg.body.startsWith(Main.getBotTrigger() + "help")) {
                log.info("help ");
                webSocket.sendMessage(msg.conversationID, "commands are: "+ Main.getBotTrigger() + "ban, "+ Main.getBotTrigger() + "help, "+ Main.getBotTrigger() + "repeat, "+ Main.getBotTrigger() + "api, will try to delete things containing \"autodeletetest\" ");
            } else if (msg.body.contains("autodeletetest") && !msg.isDeleted && !isOwner(msg.senderRoles) && !msg.body.startsWith("commands are:")) {
                log.info("autodelete " + msg.body);
                if (msg.conversationType == ConversationType.GROUP || msg.conversationType == ConversationType.FRIENDSHIP) {
                    CurseApp.deleteMessage(msg.conversationID.serialize(), msg.serverID.serialize(), msg.timestamp, Main.getToken().get());
                }
            }
        }
    }

    public boolean isOwner (@Nonnull int[] roles) {
        for (int r : roles) {
            if (r == 1) {
                return true;
            }
        }
        return false;
    }
}
