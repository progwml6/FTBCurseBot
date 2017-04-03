package com.feed_the_beast.ftbcurseappbot.runnables;

import com.beust.jcommander.internal.Lists;
import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.data.MongoCurseforgeCheck;
import com.feed_the_beast.javacurselib.addondumps.Addon;
import com.feed_the_beast.javacurselib.addondumps.AddonDatabase;
import com.feed_the_beast.javacurselib.addondumps.Bz2Data;
import com.feed_the_beast.javacurselib.addondumps.DatabaseType;
import com.feed_the_beast.javacurselib.addondumps.Filtering;
import com.feed_the_beast.javacurselib.addondumps.MergedDatabase;
import com.feed_the_beast.javacurselib.addondumps.ReleaseType;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.utils.ChatFormatter;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

@Slf4j
public class CurseforgeChecker implements Runnable {
    private WebSocket webSocket;
    private boolean initialized = false;
    private Optional<List<String>> channelsEnabled;//TODO make sure this gets updates!!
    private String types = "";

    public CurseforgeChecker (@Nonnull WebSocket webSocket) {
        this.webSocket = webSocket;
        this.channelsEnabled = Optional.of(Lists.newArrayList());
        channelsEnabled.get().add("Progwml6's mods.curseforge-updates");
    }

    public static String getFeed (ReleaseType r) {
        switch (r) {
        case ALPHA:
            return " alpha";
        case BETA:
            return " beta";
        case RELEASE:
            return " release";
        default:
            return " UNKNOWN " + (r == null ? "null" : r.getValue());
        }
    }

    private static String getChangeTextForAddon (@Nonnull Addon a) {
        StringBuilder ret = new StringBuilder();
        int i = 0;
        for (int j = 0; j < a.latestFiles.size(); j++) {
            if (a.latestFiles.get(j).fileDate.getTime() > a.latestFiles.get(i).fileDate.getTime()) {
                i = j;
            }
        }
        ret.append(a.name).append(getFeed(a.latestFiles.get(i).releaseType)).append(" for MC: ");
        for (String s : a.latestFiles.get(i).gameVersion) {
            if (!ret.toString().endsWith(", ") && !ret.toString().endsWith(": ")) {
                ret.append(", ");
            }
            ret.append(s);
        }
        /*if (!ret.endsWith(", ") && !ret.endsWith(": ")) {
            ret += " ";
        }*/
        return ret.toString();
    }

    private static String getTextForType (@Nonnull String type, @Nonnull List<Addon> lst) {
        StringBuilder result = new StringBuilder();
        if (!lst.isEmpty()) {
            result.append(ChatFormatter.bold(type)).append(": ");
            for (Addon a : lst) {
                if (!result.toString().endsWith(": ") && !result.toString().endsWith(", ")) {
                    result.append(", ");
                }
                result.append(getChangeTextForAddon(a));
            }
            if (!result.toString().endsWith(" ")) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    private static String getTextForType (@Nonnull String type, @Nonnull AddonDatabase db) {
        List<Addon> lst = Filtering.byCategorySection(type, db);
        return getTextForType(type, lst);
    }

    @Override
    public void run () {
        try {
            boolean changed = false;
            Thread.currentThread().setName("curseforgecheckthread");
            String result = "";
            types = "";
            String base = ChatFormatter.underscore(ChatFormatter.bold("Curse Updates")) + ": ";
            if (!initialized) {
                initialized = true;
                Main.getCacheService().setAddonDatabase(Bz2Data.getInitialDatabase(Bz2Data.MC_GAME_ID));
                String size = "";
                long timestamp = -1;
                if (Main.getCacheService().getAddonDatabase() == null || Main.getCacheService().getAddonDatabase().data == null) {
                    size = "null";
                } else {
                    size = String.valueOf(Main.getCacheService().getAddonDatabase().data.size());
                }
                if (Main.getCacheService().getAddonDatabase() != null) {
                    timestamp = Main.getCacheService().getAddonDatabase().timestamp;
                }
                log.info("Curseforge Checker Initialized with " + size + " entries timestamp: " + timestamp);
            } else {
                Bz2Data.debug = false;//when true this produces log spam
                MergedDatabase db = Bz2Data.updateCompleteDatabaseIfNeeded(Main.getCacheService().getAddonDatabase(), Bz2Data.MC_GAME_ID);
                if (db.changes != null && !db.changes.data.isEmpty()) {
                    Main.getCacheService().setAddonDatabase(db.currentDatabase);
                    Optional<List<MongoCurseforgeCheck>> extraChecksList = Optional.of(Lists.newArrayList());
                    if (MongoConnection.isPersistanceEnabled()) {
                        extraChecksList = MongoConnection.getCurseChecks();
                    }
                    StringBuilder dbt = new StringBuilder();
                    for (DatabaseType d : db.newDBTypes) {
                        dbt.append(d.getStringForUrl()).append(" ");
                    }
                    types = dbt.toString();
                    if (Config.isDebugEnabled()) {
                        log.debug(db.changes.data.size() + " curseforge changes detected " + dbt);
                    }
                    changed = true;
                    result = base;
                    result += getTextForType("Mods", db.changes);
                    result += getTextForType("Addons", db.changes);
                    result += getTextForType("ModPacks", db.changes);
                    result += getTextForType("Texture Packs", db.changes);
                    if (extraChecksList.isPresent()) {
                        for (MongoCurseforgeCheck m : extraChecksList.get()) {
                            if (m.getType() != null) {
                                List<Addon> ret = Filtering.byAuthorAndCategorySection(m.getAuthor(), m.getType(), db.changes);
                                String toSend = base + getTextForType(m.getType(), ret);
                                if (ret.size() > 0) {
                                    log.debug("sending {} {} to {} using {}", m.getAuthor(), m.getType(), m.getChannelID(), dbt.toString());
                                    Main.sendMessage(m.getChannelIDAsGUID(), toSend);
                                }
                            } else {
                                if (m.getAuthor() != null) {
                                    List<Addon> ret = Filtering.byAuthor(m.getAuthor(), db.changes);
                                    AddonDatabase d = new AddonDatabase();
                                    d.data = ret;
                                    d.timestamp = db.changes.timestamp;
                                    String toSend = base + getTextForType("Mods", d) + getTextForType("Addons", d) + getTextForType("Modpacks", d) + getTextForType("Texture Packs", d);
                                    if (ret.size() > 0) {
                                        log.debug("sending {} to {} using {}", m.getAuthor(), m.getChannelID(), dbt.toString());
                                        Main.sendMessage(m.getChannelIDAsGUID(), toSend);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (changed) {
                sendServiceStatusNotifications(Main.getCacheService().getContacts().get(), webSocket, result, this.channelsEnabled);
            } else {
                if (Config.isDebugEnabled()) {
                    long now = new Date().getTime();
                    log.debug("No curseforge change detected db_timestamp: " + Main.getCacheService().getAddonDatabase().timestamp + " Now: " + now + " Diff: " + (now - Main.getCacheService()
                            .getAddonDatabase().timestamp));
                }
            }
        } catch (Exception e) {
            log.error("curseforge checker exception", e);
        }
    }

    private void sendServiceStatusNotifications (@Nonnull ContactsResponse cr, @Nonnull WebSocket ws, @Nonnull String message, @Nonnull Optional<List<String>> channelsEnabled) {
        if (message.isEmpty()) {
            if (Config.isDebugEnabled()) {
                log.debug("no CurseForge Updates");
            }
            return;
        }
        channelsEnabled.ifPresent(strings -> {
            for (String s : strings) {
                if (s.contains(".")) {
                    String[] g = s.split("\\.");
                    Optional<CurseGUID> ci = Main.getCacheService().getContacts().get().getChannelIdbyNames(g[0], g[1], true);
                    if (ci.isPresent()) {
                        if (Config.isDebugEnabled()) {
                            log.debug("sending status change for {} to {} guid: {} types {}", "CurseForge", s, ci.get().serialize(), types);
                        }
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
        });
    }

}
