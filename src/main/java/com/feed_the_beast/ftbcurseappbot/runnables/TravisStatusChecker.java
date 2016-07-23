package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.globalCommands.TravisStatus;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TravisStatusChecker extends ServiceStatusBase {

    public TravisStatusChecker (WebSocket webSocket) {
        super(webSocket, TravisStatus.getInstance(), Main.getTravisStatusChangeNotificationsEnabled());
    }

}