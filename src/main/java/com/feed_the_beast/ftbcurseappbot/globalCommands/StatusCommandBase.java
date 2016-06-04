package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.javacurselib.examples.app_v1.CurseApp;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

@Slf4j
public abstract class StatusCommandBase extends CommandBase {
    public abstract String getService ();

    public Pattern getTriggerRegex () {
        return getSimpleCommand(getService() + "status");
    }

    @Nonnull
    public abstract String getServiceStatus ();

    @Nonnull
    public abstract String updateServiceHealth ();

    public void sendServiceStatusNotifications (@Nonnull ContactsResponse cr, @Nonnull WebSocket ws, @Nonnull String message, @Nonnull java.util.Optional<List<String>> channelsEnabled) {
        if (message.isEmpty()) {
            log.info("no change in " + getService() + " health status");
            return;
        }
        if (channelsEnabled.isPresent()) {
            for (String s : channelsEnabled.get()) {
                if (s.contains(".")) {
                    String[] g = s.split("\\.");
                    ws.sendMessage(CurseApp.getChangelIDFromChannelName(cr, g[0], g[1]), message);
                } else {
                    ws.sendMessage(CurseApp.getChangelIDFromChannelName(cr, null, s), message);
                }
            }
        }
    }
}
