package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.ftbcurseappbot.api.ICommandBase;
import com.feed_the_beast.javacurselib.common.enums.ConversationNotificationType;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.handler.tasks.Task;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;

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
            } else if (msg.body.startsWith(Config.getBotTrigger() + "api")) {
                log.info("api " + msg.body.replace(Config.getBotTrigger() + "api", ""));
                webSocket.sendMessage(msg.conversationID, "CurseApp api is located at http://api.feed-the-beast.com/curseapiaccess.php");
            } else {//custom server commands
                if (Main.getSession().isPresent() && msg.senderID != Main.getSession().get().user.userID) {//bot can't execute commands
                    CommandRegistry.processServerCommands(msg.rootConversationID, webSocket, msg);
                }
            }
            webSocket.sendMarkRead(msg.conversationID);
        }
    }

}
