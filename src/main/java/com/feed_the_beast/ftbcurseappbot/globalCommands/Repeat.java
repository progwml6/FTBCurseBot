package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class Repeat extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("repeat " + msg.body.replace(Config.getBotTrigger() + "repeat", ""));
        Main.sendMessage(msg.conversationID, msg.body.replace(Config.getBotTrigger() + "repeat ", "").replace(Config.getBotTrigger() + "repeat", ""));

    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("repeat");
    }

    @Override
    public String getHelp () {
        return "repeats event message";
    }
}
