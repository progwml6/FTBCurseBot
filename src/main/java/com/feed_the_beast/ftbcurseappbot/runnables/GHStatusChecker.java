package com.feed_the_beast.ftbcurseappbot.runnables;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.globalCommands.GHStatus;
import com.feed_the_beast.ftbcurseappbot.globalCommands.StatusCommandBase;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

@Slf4j
public class GHStatusChecker extends ServiceStatusBase {

    public GHStatusChecker (@Nonnull WebSocket webSocket, @Nonnull ContactsResponse contactsResponse) {
        super(webSocket, contactsResponse, GHStatus.getInstance(), Main.getGHStatusChangeNotificationsEnabled());
    }
}
