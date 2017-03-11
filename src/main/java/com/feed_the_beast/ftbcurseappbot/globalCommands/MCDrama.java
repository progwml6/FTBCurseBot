package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.utils.NetworkingUtils;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class MCDrama extends CommandBase {
    private static String getDrama () {
        String drama = null;
        try {
            drama = NetworkingUtils.getSynchronous("http://mc-drama.herokuapp.com/raw");
        } catch (Exception e) {
            log.error("error getting mc drama ", e);
        }
        return drama;
    }

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        Main.sendMessage(msg.conversationID, getDrama());
        log.debug("got drama");
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("mcdrama");
    }

    @Override
    public String getHelp () {
        return "gets mc drama";
    }

}
