package com.feed_the_beast.ftbcurseappbot.runnables;

import com.beust.jcommander.internal.Lists;
import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.addondumps.Addon;
import com.feed_the_beast.javacurselib.addondumps.Bz2Data;
import com.feed_the_beast.javacurselib.addondumps.DatabaseType;
import com.feed_the_beast.javacurselib.addondumps.MergedDatabase;
import com.feed_the_beast.javacurselib.addondumps.ReleaseType;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
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

    public CurseforgeChecker (@Nonnull WebSocket webSocket) {
        this.webSocket = webSocket;
        this.channelsEnabled = Optional.of(Lists.newArrayList());
        channelsEnabled.get().add("Progwml6's mods.curseforge-updates");
    }

    @Override
    public void run () {
        try {
            boolean changed = false;
            Thread.currentThread().setName("curseforgecheckthread");
            String result = "";
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
                MergedDatabase db = Bz2Data.updateCompleteDatabaseIfNeeded(Main.getCacheService().getAddonDatabase(), Bz2Data.MC_GAME_ID);
                if (db.changes != null) {
                    String dbt = "";
                    for (DatabaseType d : db.newDBTypes) {
                        dbt += d.getStringForUrl() + " ";
                    }
                    log.debug(db.changes.data.size() + "curseforge changes detected " + dbt);
                    changed = true;
                    result = "_*Curse Updates*_: ";
                    String mods = "";
                    String packs = "";
                    String tps = "";
                    for (Addon a : db.changes.data) {
                        if (a.categorySection.path.equals("mods")) {
                            if (mods.isEmpty()) {
                                mods += "*Mods*: ";
                            } else {
                                mods += ", ";
                            }
                            mods += a.name + getFeed(a.latestFiles.get(0).releaseType) + " for MC: ";
                            for (String s : a.latestFiles.get(0).gameVersion) {
                                if (!mods.endsWith(", ") && !mods.endsWith(": ")) {
                                    mods += ", ";
                                }
                                mods += s;
                            }
                            if (!mods.endsWith(", ") && !mods.endsWith(": ")) {
                                mods += " ";
                            }

                        } else if (a.categorySection.path.equals("resourcepacks")) {
                            if (tps.isEmpty()) {
                                tps += "*Resource Packs*: ";
                            } else {
                                tps += ", ";
                            }
                            tps += a.name + getFeed(a.latestFiles.get(0).releaseType) + " for MC: ";
                            for (String s : a.latestFiles.get(0).gameVersion) {
                                if (!tps.endsWith(", ") && !tps.endsWith(": ")) {
                                    tps += ", ";
                                }
                                tps += s;
                            }
                            if (!tps.endsWith(", ") && !tps.endsWith(": ")) {
                                tps += " ";
                            }

                        } else if (a.categorySection.name.equals("Modpacks")) {
                            if (packs.isEmpty()) {
                                packs += "*ModPacks*: ";
                            } else {
                                packs += ", ";
                            }
                            packs += a.name + getFeed(a.latestFiles.get(0).releaseType) + " for MC: ";
                            for (String s : a.latestFiles.get(0).gameVersion) {
                                if (!packs.endsWith(", ") && !packs.endsWith(": ")) {
                                    packs += ", ";
                                }
                                packs += s;
                            }
                            if (!packs.endsWith(", ") && !packs.endsWith(": ")) {
                                packs += " ";
                            }

                        }
                    }
                    result += mods;
                    if (!result.endsWith(": ")) {
                        result += ", ";
                    }
                    result += packs;
                    if (!result.endsWith(": ") || !result.endsWith(", ")) {
                        result += ", ";
                    }
                    result += tps;
                    Main.getCacheService().setAddonDatabase(db.currentDatabase);
                }
            }
            if (changed) {
                sendServiceStatusNotifications(Main.getCacheService().getContacts().get(), webSocket, result, this.channelsEnabled);
            } else {
                long now = new Date().getTime();
                log.debug("No curseforge change detected db_timestamp: " + Main.getCacheService().getAddonDatabase().timestamp + " Now: " + now + " Diff: " + (now - Main.getCacheService()
                        .getAddonDatabase().timestamp));
            }
        } catch (Exception e) {
            log.error("curseforge checker exception", e);
        }
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
            return " UNKNOWN " + r.getValue();
        }
    }

    public void sendServiceStatusNotifications (@Nonnull ContactsResponse cr, @Nonnull WebSocket ws, @Nonnull String message, @Nonnull java.util.Optional<List<String>> channelsEnabled) {
        if (message.isEmpty()) {
            if (Config.isDebugEnabled()) {
                log.debug("no CurseForge Updates");
            }
            return;
        }
        if (channelsEnabled.isPresent()) {
            log.info("{} has had an update change");
            for (String s : channelsEnabled.get()) {
                log.info("sending {} change to {}", "CurseForge", s);
                if (s.contains(".")) {
                    String[] g = s.split("\\.");
                    Optional<CurseGUID> ci = Main.getCacheService().getContacts().get().getChannelIdbyNames(g[0], g[1], true);
                    if (ci.isPresent()) {
                        log.debug("sending status change for {} to {} guid: {}", "CurseForge", s, ci.get().serialize());
                        ws.sendMessage(ci.get(), message);
                    } else {
                        log.error("no channel id exists for {} {}", g[0], g[1]);
                    }
                } else {
                    Optional<CurseGUID> ci = Main.getCacheService().getContacts().get().getGroupIdByName(s, String::equalsIgnoreCase);
                    if (ci.isPresent()) {
                        ws.sendMessage(ci.get(), message);
                    } else {
                        log.error("no channel id exists for {}", s);
                    }

                }
            }
        }
    }

}
