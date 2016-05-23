package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.ApiStatus;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.StatusApiUrls;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.StatusMessage;
import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import com.feed_the_beast.javacurselib.examples.app_v1.CurseApp;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

@Slf4j
public class GHStatus extends CommandBase {

    private static StatusApiUrls urls;
    private static ApiStatus apiStatus;
    private static StatusMessage message;

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("ghstatus " + msg.body.replace(Main.getBotTrigger() + "ghstatus", ""));
        webSocket.sendMessage(msg.conversationID, getGHStatus());

    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("ghstatus");
    }

    @Override
    public String getHelp () {
        return "gets GitHub status";
    }

    public static String getGHStatus () {
        updateGHHealth();
        if (message == null || apiStatus == null) {
            return "ERROR getting GH status";
        }
        String ret;
        if(!apiStatus.getStatus().equals(message.getStatus())) {
            ret = "Discrepency between GH apis status: " + apiStatus.getStatus() + " last message: "
                    + message.getStatus() + " " + message.getBody();
        } else {
            ret = "GH Status: " + message.getStatus() + " " + message.getBody();
        }
        return ret.replace("major", ":negative_squared_cross_mark:").replace("minor", ":construction:")
                .replace("good", ":white_check_mark:");
    }

    public static @Nonnull String updateGHHealth() {
        String ret;
        try {
            if (urls == null) {
                urls = JsonFactory.GSON.fromJson(Jsoup.connect("https://status.github.com/api.json").ignoreContentType(true).get().text(), StatusApiUrls.class);
            }
            ApiStatus status = JsonFactory.GSON.fromJson(Jsoup.connect(urls.getStatus_url()).ignoreContentType(true).get().text(), ApiStatus.class);
            StatusMessage lastMessage = JsonFactory.GSON.fromJson(Jsoup.connect(urls.getLast_message_url()).ignoreContentType(true).get().text(), StatusMessage.class);
            boolean statusChanged = false;
            boolean messageChanged = false;
            if(apiStatus == null) {
                apiStatus = status;
            }
            if(message == null) {
                message = lastMessage;
            }
            if(apiStatus.equals(status)) {
                log.info("gh status hasn't changed");
            } else {
                statusChanged = true;
            }
            if(message.equals(lastMessage)){
                log.info("lastMessage is the same");
            } else {
                messageChanged = true;
            }
            if(status.getStatus().equals(lastMessage.getStatus())) {
                ret = "GH Status: " + lastMessage.getStatus() + " " + lastMessage.getBody();
            } else {
                ret = "Discrepency between GH apis status: " + status.getStatus() + " last message: "
                        + lastMessage.getStatus() + " " + lastMessage.getBody();
            }
            apiStatus = status;
            message = lastMessage;
        }catch(IOException e) {
            log.error("error getting gh status", e);
            ret = "Error getting GH status";
        }
        return ret.replace("major", ":negative_squared_cross_mark:").replace("minor", ":construction:")
                .replace("good", ":white_check_mark:");
    }
    public static void sendGHStatusNotifications (@Nonnull ContactsResponse cr, @Nonnull WebSocket ws, @Nonnull String message) {
        if (message.isEmpty()) {
            log.info("no change in gh health status");
            return;
        }
        if (Main.getGHStatusChangeNotificationsEnabled().isPresent()) {
            for (String s : Main.getGHStatusChangeNotificationsEnabled().get()) {
                if (s.contains(".")) {
                    String[] g = s.split("\\.");
                    ws.sendMessage(CurseApp.getChangelIDFromChannelName(cr, g[0], g[1]), message);
                } else {
                    ws.sendMessage(CurseApp.getChangelIDFromChannelName(cr, null, s), message);
                }
            }
        }
    }

}
