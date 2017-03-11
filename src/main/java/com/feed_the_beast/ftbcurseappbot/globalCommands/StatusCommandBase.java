package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
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

    public abstract boolean hasChanged ();

    public void sendServiceStatusNotifications (@Nonnull ContactsResponse cr, @Nonnull WebSocket ws, @Nonnull String message, @Nonnull java.util.Optional<List<String>> channelsEnabled) {
        if (message.isEmpty()) {
            if (Config.isDebugEnabled()) {
                log.debug("no change in {} health status", getService());
            }
            return;
        }
        if (channelsEnabled.isPresent()) {
            log.info("{} has had a status change", getService());
            for (String s : channelsEnabled.get()) {
                if (s.contains(".")) {
                    String[] g = s.split("\\.");
                    Optional<CurseGUID> ci = Main.getCacheService().getContacts().get().getChannelIdbyNames(g[0], g[1], true);
                    if (ci.isPresent()) {
                        log.debug("sending status change for {} to {} guid: {}", getService(), s, ci.get().serialize());
                        Main.sendMessage(ci.get(), message);
                    } else {
                        log.error("no channel id exists for {} {}", g[0], g[1]);
                    }
                } else {
                    Optional<CurseGUID> ci = Main.getCacheService().getContacts().get().getGroupIdByName(s, String::equalsIgnoreCase);
                    if (ci.isPresent()) {
                        Main.sendMessage(ci.get(), message);
                    } else {
                        log.error("no channel id exists for {}", s);
                    }

                }
            }
        }
    }
}
