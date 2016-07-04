package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.ftbcurseappbot.persistence.data.MongoCommand;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

/**
 * Created by progwml6 on 7/4/16.
 */
@Slf4j
public class CustomCommandExecutor {
    private CustomCommandExecutor () {

    }

    /**
     * executes custom server commands
     * @param cmd mongo command
     * @param webSocket websocket instance
     * @param msg original message
     * @return true if message was sent, false otherwise
     */
    public static boolean execute (@Nonnull MongoCommand cmd, @Nonnull WebSocket webSocket, @Nonnull ConversationMessageNotification msg) {
        if (msg.senderPermissions.containsAll(cmd.getGroupPermissions())) {
            webSocket.sendMessage(msg.conversationID, cmd.getContent());
            log.info("executing command {} on server {}", cmd.getRegex(), msg.serverID);
            return true;
        } else {
            //TODO log permission violations in mongo
        }
        return false;
    }
}
