package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.globalCommands.MCStatus;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class McStatusChecker implements Runnable {
    private WebSocket webSocket;
    private ContactsResponse contactsResponse;
    private boolean initialized = false;

    public McStatusChecker(WebSocket webSocket, ContactsResponse contactsResponse) {
        this.webSocket = webSocket;
        this.contactsResponse = contactsResponse;
    }

    @Override
    public void run() {
        String result = MCStatus.updateMCHealth();
        if (!initialized) {
            log.info("MC status initialized");
            initialized = true;
            return;
        }

        MCStatus.sendMCHealthChangeNotifications(contactsResponse, webSocket, result);
    }
}
