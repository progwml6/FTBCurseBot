package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class Help extends CommandBase {

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("help " + msg.senderName);
        webSocket.sendMessage(msg.conversationID,
                "commands are: " + Main.getBotTrigger() + "ban, " + Main.getBotTrigger() + "bbstatus, " + Main.getBotTrigger() + "cfstatus, " + Main.getBotTrigger() + "chatformatting, " + Main
                        .getBotTrigger() + "customcommands, " + Main.getBotTrigger() + "delcmd, " + Main.getBotTrigger() + "ftbfaq, " + Main.getBotTrigger() + "ghstatus, " + Main.getBotTrigger()
                        + "ftbbot, " + Main.getBotTrigger() + "help, " + Main.getBotTrigger() + "haspaidmc, " + Main.getBotTrigger() + "ipban, " + Main.getBotTrigger() + "kick, " + Main
                        .getBotTrigger() + "repeat, " + Main.getBotTrigger() + "mcstatus, " + Main.getBotTrigger() + "mcuuid, " + Main.getBotTrigger() + "shorten, " + Main.getBotTrigger() + "setcmd, "
                        + Main.getBotTrigger() + "mcdrama, " + Main.getBotTrigger() + "travisstatus, " + Main.getBotTrigger() + "api");
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("help");
    }

}
