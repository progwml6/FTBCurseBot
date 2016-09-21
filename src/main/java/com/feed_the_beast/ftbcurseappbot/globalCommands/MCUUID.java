package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.utils.NetworkingUtils;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
public class MCUUID extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("mcuuid " + msg.body);
        String[] msplit = msg.body.split(" ");
        if (msplit.length > 1) {
            log.info("mcuuid " + msplit[1]);
            try {
                String json = NetworkingUtils.getSynchronous("https://api.mojang.com/users/profiles/minecraft/" + msplit[1]);
                JsonParser p = new JsonParser();
                JsonElement report = p.parse(json);
                if (report != null && report.isJsonObject()) {
                    webSocket.sendMessage(msg.conversationID, "Username: " + report.getAsJsonObject().get("name").getAsString() + " UUID: " + report.getAsJsonObject().get("id").getAsString());
                } else {
                    webSocket.sendMessage(msg.conversationID, "Could not find UUID for " + msplit[1]);

                }
            } catch (IOException | JsonParseException e) {
                webSocket.sendMessage(msg.conversationID, "Was unable to check with mojang for " + msplit[1] + "'s uuid!");
            }
        } else {
            webSocket.sendMessage(msg.conversationID, "You need to enter a username!");
        }
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("mcuuid");
    }

    @Override
    public String getHelp () {
        return "mcuuid <USERNAME>";
    }
}
