package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.globalCommands.CFStatus;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CFStatusChecker implements Runnable {
    private WebSocket webSocket;
    private ContactsResponse contactsResponse;//TODO make sure this gets refreshed periodically
    private boolean initialized = false;

    public CFStatusChecker (WebSocket webSocket, ContactsResponse contactsResponse) {
        this.webSocket = webSocket;
        this.contactsResponse = contactsResponse;
    }

    @Override
    public void run () {
        String result = CFStatus.updateCFHealth();
        if (!initialized) {
            log.info("CF status initialized");
            initialized = true;
            return;
        }

        CFStatus.sendCFStatusNotifications(contactsResponse, webSocket, result);
    }

}