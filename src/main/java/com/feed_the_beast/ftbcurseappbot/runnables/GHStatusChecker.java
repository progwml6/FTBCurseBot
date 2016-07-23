package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.globalCommands.GHStatus;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

@Slf4j
public class GHStatusChecker extends ServiceStatusBase {

    public GHStatusChecker (@Nonnull WebSocket webSocket) {
        super(webSocket, GHStatus.getInstance(), Main.getGHStatusChangeNotificationsEnabled());
    }
}
