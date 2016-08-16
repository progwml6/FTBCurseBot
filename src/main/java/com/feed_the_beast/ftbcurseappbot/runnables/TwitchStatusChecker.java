package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.globalCommands.TwitchStatus;
import com.feed_the_beast.javacurselib.websocket.WebSocket;

/**
 * Created by progwml6 on 8/16/16.
 */
public class TwitchStatusChecker extends ServiceStatusBase {
    public TwitchStatusChecker (WebSocket webSocket) {
        super(webSocket, TwitchStatus.instance, Main.getTwitchStatusChangeNotificationsEnabled());
    }

}
