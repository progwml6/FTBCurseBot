package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.globalCommands.StatusCommandBase;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

@Slf4j
public class ServiceStatusBase implements Runnable {
    private WebSocket webSocket;
    private ContactsResponse contactsResponse;//TODO make sure this gets refreshed periodically
    private boolean initialized = false;
    private StatusCommandBase statusCommand;
    private Optional<List<String>> channelsEnabled;//TODO make sure this gets updates!!

    public ServiceStatusBase (@Nonnull WebSocket webSocket, @Nonnull ContactsResponse contactsResponse, @Nonnull StatusCommandBase command, @Nonnull Optional<List<String>> channelsEnabled) {
        this.webSocket = webSocket;
        this.contactsResponse = contactsResponse;
        this.channelsEnabled = channelsEnabled;
        this.statusCommand = command;
    }

    @Override
    public void run () {
        String result = statusCommand.updateServiceHealth();
        if (!initialized) {
            initialized = true;
            log.info(statusCommand.getService() + " health initialized");
            return;
        }
        statusCommand.sendServiceStatusNotifications(contactsResponse, webSocket, result, this.channelsEnabled);
    }

}
