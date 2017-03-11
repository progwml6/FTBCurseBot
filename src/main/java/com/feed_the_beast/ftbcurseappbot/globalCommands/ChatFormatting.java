package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;

import java.util.regex.Pattern;

public class ChatFormatting extends CommandBase {
    private static final String CURSE_SUPPORT_CHAT_FORMATTING = "https://support.curse.com/hc/en-us/articles/210356803-Text-Chat-Message-Formatting";

    @Override public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        Main.sendMessage(msg.conversationID, CURSE_SUPPORT_CHAT_FORMATTING);
    }

    @Override public Pattern getTriggerRegex () {
        return getSimpleCommand("chatformatting");
    }

    @Override public String getHelp () {
        return "!chatformatting gives info about Curse chat formatting";
    }
}

