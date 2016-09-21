package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.globalCommands.StatusCommandBase;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

@Slf4j
public class ServiceStatusBase implements Runnable {
    private WebSocket webSocket;
    private boolean initialized = false;
    private StatusCommandBase statusCommand;
    private Optional<List<String>> channelsEnabled;//TODO make sure this gets updates!!

    public ServiceStatusBase (@Nonnull WebSocket webSocket, @Nonnull StatusCommandBase command, @Nonnull Optional<List<String>> channelsEnabled) {
        this.webSocket = webSocket;
        this.channelsEnabled = channelsEnabled;
        this.statusCommand = command;
    }

    @Override
    public void run () {
        Thread.currentThread().setName(statusCommand.getService() + "statusthread");
        String result = statusCommand.updateServiceHealth();
        if (!initialized) {
            initialized = true;
            log.info(statusCommand.getService() + " health initialized");
            return;
        }
        if (statusCommand.hasChanged()) {
            statusCommand.sendServiceStatusNotifications(Main.getCacheService().getContacts().get(), webSocket, result, this.channelsEnabled);
        }
    }

}
