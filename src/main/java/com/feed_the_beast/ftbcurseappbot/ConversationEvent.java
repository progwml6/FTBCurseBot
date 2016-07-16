package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.ftbcurseappbot.api.ICommandBase;
import com.feed_the_beast.javacurselib.common.enums.ConversationNotificationType;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.handler.tasks.Task;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Created by progwml6 on 5/7/16.
 */
@Slf4j
public class ConversationEvent implements Task<ConversationMessageNotification> {
    @Override
    public void execute (@Nonnull WebSocket webSocket, @Nonnull ConversationMessageNotification msg) {
        if (msg.notificationType == ConversationNotificationType.NORMAL || msg.notificationType == ConversationNotificationType.EDITED) {
            Optional<ICommandBase> command = CommandRegistry.getCommand(msg.rootConversationID, msg.body);
            if (command.isPresent() && Main.getSession().isPresent() && msg.senderID != Main.getSession().get().user.userID) {//bot cannot execute commands for security reasons
                command.get().onMessage(webSocket, msg);
            } else if (msg.body.startsWith(Main.getBotTrigger() + "api")) {
                log.info("api " + msg.body.replace(Main.getBotTrigger() + "api", ""));
                webSocket.sendMessage(msg.conversationID, "CurseApp api is located at http://api.feed-the-beast.com/curseapiaccess.php");
                // REST.conversations.deleteMessage(msg.conversationID, msg.serverID, msg.timestamp);//this actually needs serverID from the conversation
            } else {//custom server commands
                if (Main.getSession().isPresent() && msg.senderID != Main.getSession().get().user.userID) {//bot can't execute commands
                    CommandRegistry.processServerCommands(msg.rootConversationID, webSocket, msg);
                }
            }
            webSocket.sendMarkRead(msg.conversationID);
        }
    }

}
