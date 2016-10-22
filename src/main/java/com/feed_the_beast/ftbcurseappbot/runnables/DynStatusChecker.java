package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.globalCommands.DynStatus;
import com.feed_the_beast.javacurselib.websocket.WebSocket;

public class DynStatusChecker extends ServiceStatusBase {

    public DynStatusChecker (WebSocket webSocket) {
        super(webSocket, DynStatus.getInstance(), Config.getDynStatusChangeNotificationsEnabled());
    }

}