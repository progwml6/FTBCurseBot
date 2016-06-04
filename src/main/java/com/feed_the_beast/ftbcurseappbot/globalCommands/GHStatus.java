package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.ApiStatus;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.StatusApiUrls;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.StatusMessage;
import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;

import javax.annotation.Nonnull;

@Slf4j
public class GHStatus extends StatusCommandBase {
    public static StatusCommandBase instance;

    public GHStatus () {
        instance = this;
    }

    public static StatusCommandBase getInstance(){
        return instance;
    }

    private static StatusApiUrls urls;
    private static ApiStatus apiStatus;
    private static StatusMessage message;

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("ghstatus " + msg.body.replace(Main.getBotTrigger() + "ghstatus", ""));
        webSocket.sendMessage(msg.conversationID, getServiceStatus());

    }

    @Override
    public String getService () {
        return "gh";
    }

    @Override
    public String getHelp () {
        return "gets GitHub status";
    }

    @Override
    public String getServiceStatus () {
        updateServiceHealth();
        if (message == null || apiStatus == null) {
            return "ERROR getting GH status";
        }
        String ret;
        if (!apiStatus.getStatus().equals(message.getStatus())) {
            ret = "Discrepency between GH apis status: " + apiStatus.getStatus() + " last message: "
                    + message.getStatus() + " " + message.getBody();
        } else {
            ret = "GH Status: " + message.getStatus() + " " + message.getBody();
        }
        return ret.replace("major", ":negative_squared_cross_mark:").replace("minor", ":construction:")
                .replace("good", ":white_check_mark:");
    }

    @Override
    public @Nonnull String updateServiceHealth () {
        String ret;
        try {
            if (urls == null) {
                urls = JsonFactory.GSON.fromJson(Jsoup.connect("https://status.github.com/api.json").ignoreContentType(true).get().text(), StatusApiUrls.class);
            }
            ApiStatus status = JsonFactory.GSON.fromJson(Jsoup.connect(urls.getStatus_url()).ignoreContentType(true).get().text(), ApiStatus.class);
            StatusMessage lastMessage = JsonFactory.GSON.fromJson(Jsoup.connect(urls.getLast_message_url()).ignoreContentType(true).get().text(), StatusMessage.class);
            boolean statusChanged = false;
            boolean messageChanged = false;
            if (apiStatus == null) {
                apiStatus = status;
            }
            if (message == null) {
                message = lastMessage;
            }
            if (apiStatus.equals(status)) {
                log.info("gh status hasn't changed");
            } else {
                statusChanged = true;
            }
            if (message.equals(lastMessage)) {
                log.info("lastMessage is the same from GH");
            } else {
                messageChanged = true;
            }
            if (status.getStatus().equals(lastMessage.getStatus())) {
                ret = "GH Status: " + lastMessage.getStatus() + " " + lastMessage.getBody();
            } else {
                ret = "Discrepency between GH apis status: " + status.getStatus() + " last message: "
                        + lastMessage.getStatus() + " " + lastMessage.getBody();
            }
            apiStatus = status;
            message = lastMessage;
        } catch (IOException e) {
            log.error("error getting gh status", e);
            ret = "Error getting GH status";
        }
        return ret.replace("major", ":negative_squared_cross_mark:").replace("minor", ":construction:")
                .replace("good", ":white_check_mark:");
    }

}
