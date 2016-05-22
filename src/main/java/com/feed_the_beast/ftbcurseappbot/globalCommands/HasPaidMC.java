package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
public class HasPaidMC extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        String message = msg.body;
        String[] msplit = msg.body.split(" ");
        if (msplit.length > 1) {
            log.info("haspaidmc " + msplit[1]);
            try {
                if(getHasPaid(msplit[1])) {
                    webSocket.sendMessage(msg.conversationID, msplit[1] +  " has paid for minecraft :white_check_mark:");
                } else {
                    webSocket.sendMessage(msg.conversationID, msplit[1] +  " has NOT paid for minecraft :negative_squared_cross_mark:");
                }
            } catch (IOException e) {
                webSocket.sendMessage(msg.conversationID, "Was unable to check with mojang if " + msplit[1] + " has paid!");
            }
        } else {
            webSocket.sendMessage(msg.conversationID, "You need to enter a username!");
        }
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("haspaidmc");
    }

    @Override
    public String getHelp () {
        return "haspaidmc <USERNAME>";
    }

    public static boolean getHasPaid (String user) throws IOException {
        try {
            String json = Jsoup.connect("https://api.mojang.com/users/profiles/minecraft/" + user).ignoreContentType(true).get().text();
            JsonParser p = new JsonParser();
            JsonElement report = p.parse(json);
            if (report == null || !report.isJsonObject()) {
                return false;
            }
            return true;
        } catch (JsonParseException e) {
            return false;//TODO is this always the case
        }

    }
}
