package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
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
        Main.sendMessage(msg.conversationID,
                "commands are: " + Config.getBotTrigger() + "addcursecheck, " + Config.getBotTrigger() + "ban, " + Config.getBotTrigger() + "bbstatus, " + Config.getBotTrigger() + "cfstatus, "
                        + Config.getBotTrigger() + "chatformatting, " + Config.getBotTrigger() + "cfstatus, " + Config.getBotTrigger() + "curseuuid, " + Config.getBotTrigger() + "customcommands, "
                        + Config.getBotTrigger() + "delcmd, " + Config.getBotTrigger() + "delcursecheck, " + Config
                        .getBotTrigger() + "dynstatus, " + Config.getBotTrigger() + "ftbbot, " + Config.getBotTrigger() + "ftbfaq, " + Config.getBotTrigger() + "ghstatus, " + Config.getBotTrigger()
                        + "help, " + Config.getBotTrigger() + "haspaidmc, " + Config.getBotTrigger() + "ipban, " + Config.getBotTrigger() + "kick, " + Config.getBotTrigger() + "repeat, " + Config
                        .getBotTrigger() + "mcstatus, " + Config.getBotTrigger() + "mcuuid, " + Config.getBotTrigger() + "shorten, " + Config.getBotTrigger() + "setcmd, " + Config.getBotTrigger()
                        + "mcdrama, " + Config.getBotTrigger() + "travisstatus, " + Config.getBotTrigger() + "twitchstatus, " + Config.getBotTrigger() + "api");
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("help");
    }

}
