package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.ftbcurseappbot.api.ICommandBase;
import com.feed_the_beast.javacurselib.examples.app_v1.CurseApp;
import com.feed_the_beast.javacurselib.rest.REST;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.service.conversations.conversations.ConversationNotificationType;
import com.feed_the_beast.javacurselib.service.conversations.conversations.ConversationType;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.handler.tasks.Task;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;
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
            Optional<ICommandBase> command = CommandRegistry.getCommand(msg.conversationID.serialize(), msg.body);
            if(command.isPresent()) {
                command.get().onMessage(webSocket, msg);
            }
            else if (msg.body.startsWith(Main.getBotTrigger() + "ban")) {
                log.info("LOLBAN " + msg.body.replace(Main.getBotTrigger() + "ban", ""));
                webSocket.sendMessage(msg.conversationID, "not actually going to ban " + msg.body.replace(Main.getBotTrigger() + "ban", ""));
            } else if (msg.body.startsWith(Main.getBotTrigger() + "api")) {
                log.info("api " + msg.body.replace(Main.getBotTrigger() + "api", ""));
                webSocket.sendMessage(msg.conversationID, "CurseApp api is located at http://api.feed-the-beast.com/curseapiaccess.php");
            } else if (msg.body.contains("autodeletetest") && !msg.isDeleted && !isOwner(msg.senderRoles) && !msg.body.startsWith("commands are:")) {
                log.info("autodelete " + msg.body);
                if (msg.conversationType == ConversationType.GROUP || msg.conversationType == ConversationType.FRIENDSHIP) {
                    REST.conversations.deleteMessage(msg.conversationID, msg.serverID, msg.timestamp);
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
