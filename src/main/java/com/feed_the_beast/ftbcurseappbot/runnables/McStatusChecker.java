package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.globalCommands.MCStatus;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class McStatusChecker extends ServiceStatusBase {

    public McStatusChecker (WebSocket webSocket) {
        super(webSocket, MCStatus.getInstance(), Config.getMcStatusChangeNotificationsEnabled());
    }

}
