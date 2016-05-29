package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.util.regex.Pattern;

@Slf4j
public class Shorten extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        String[] args = msg.body.split(" ");
        if (args.length == 2) {
            log.debug("shorten url " + args[1]);
            webSocket.sendMessage(msg.conversationID, shortenURL(args[1]));
        }

    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("shorten");
    }

    @Override
    public String getHelp () {
        return "shorten [url]";
    }

    public static String shortenURL (String url) {
        try {
            String json = Jsoup.connect("http://is.gd/create.php?format=json&url=" + url).ignoreContentType(true).get().text();
            JsonParser p = new JsonParser();
            JsonElement report = p.parse(json);
            return report.getAsJsonObject().get("shorturl").getAsString();

        } catch (Exception e) {
            log.error("error shortening url: " + url, e);
        }
        return null;

    }
}
