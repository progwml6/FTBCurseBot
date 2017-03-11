package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class FTBBot extends CommandBase {
    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        Main.sendMessage(msg.conversationID, "This is an instance of FTBBot " + Main.VERSION + " which is being built by the FTB Dev team in Java. "
                + "Source Code: https://github.com/progwml6/ftbcursebot Library: https://github.com/progwml6/javacurseapi");
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("ftbbot");
    }

    @Override
    public String getHelp () {
        return "returns basic bot information";
    }
}
