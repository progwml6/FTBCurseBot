package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.ApiStatus;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.StatusApiUrls;
import com.feed_the_beast.ftbcurseappbot.api.ghstatus.StatusMessage;
import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import com.feed_the_beast.ftbcurseappbot.utils.NetworkingUtils;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import javax.annotation.Nonnull;

@Slf4j
public class GHStatus extends StatusCommandBase {
    private static StatusCommandBase instance;
    private static StatusApiUrls urls;
    private static ApiStatus apiStatus;
    private static StatusMessage message;
    private boolean statusChanged = false;
    private boolean messageChanged = false;

    public GHStatus () {
        instance = this;
    }

    public static StatusCommandBase getInstance () {
        return instance;
    }

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("ghstatus " + msg.body.replace(Config.getBotTrigger() + "ghstatus", ""));
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

    @Nonnull @Override
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
                urls = JsonFactory.GSON.fromJson(NetworkingUtils.getSynchronous("https://status.github.com/api.json"), StatusApiUrls.class);
            }
            ApiStatus status = JsonFactory.GSON.fromJson(NetworkingUtils.getSynchronous(urls.getStatus_url()), ApiStatus.class);
            StatusMessage lastMessage = JsonFactory.GSON.fromJson(NetworkingUtils.getSynchronous(urls.getLast_message_url()), StatusMessage.class);
            if (apiStatus == null) {
                apiStatus = status;
            }
            if (message == null) {
                message = lastMessage;
            }
            if (apiStatus.equals(status)) {
                if (Config.isDebugEnabled()) {
                    log.debug("gh status hasn't changed");
                }
                statusChanged = false;
            } else {
                log.info("ghstatus changed");
                statusChanged = true;
            }
            if (message.equals(lastMessage)) {
                if (Config.isDebugEnabled()) {
                    log.debug("lastMessage is the same from GH");
                }
                messageChanged = false;
            } else {
                log.info("ghMessage changed");
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

    @Override
    public boolean hasChanged () {
        return messageChanged || statusChanged;
    }

}
