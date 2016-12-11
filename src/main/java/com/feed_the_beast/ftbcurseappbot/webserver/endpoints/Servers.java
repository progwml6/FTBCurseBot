package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.data.ModerationLog;
import com.feed_the_beast.ftbcurseappbot.utils.CommonMarkUtils;
import com.feed_the_beast.javacurselib.common.enums.GroupStatus;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ChannelContract;
import com.feed_the_beast.javacurselib.service.contacts.contacts.GroupNotification;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

@Slf4j
public class Servers {
    public static Map render (@Nonnull String title) {
        Map map = Maps.newHashMap();
        map.put("commonmark", Main.getCommonMarkUtils().renderToHTML(getMdForServers()));
        map.put("titleText", title);
        return map;
    }

    //TODO enable caching for this
    public static Map renderSpecificChannel (Request req, Response response) {
        String uuid = req.params(":guid");
        if (uuid != null && !uuid.isEmpty()) {
            CurseGUID guid = CurseGUID.deserialize(uuid);
            for (GroupNotification group : Main.getCacheService().getContacts().get().groups) {
                if (group.isPublic && !group.hideNoAccess) {
                    for (ChannelContract c : group.channels) {
                        if (c.groupID.equals(guid)) {
                            if (c.isPublic && !c.hideNoAccess) {
                                Map map = Maps.newHashMap();
                                map.put("commonmark", Main.getCommonMarkUtils().renderToHTML(getMdForChannel(group, c, true, true)));
                                map.put("titleText", group.groupTitle);
                                return map;
                            } else if (!c.isPublic && !c.hideNoAccess) {
                                Map map = Maps.newHashMap();
                                map.put("commonmark", Main.getCommonMarkUtils().renderToHTML(c.groupTitle + " isn't public"));
                                map.put("titleText", group.groupTitle);
                                return map;
                            } else {
                                //TODO we need to toss a better error here!
                                return rendererror(req, response, uuid, 500);
                            }

                        }
                    }
                }
            }
        }
        return rendererror(req, response, uuid, 500);

    }

    //TODO enable caching for this
    public static Map renderSpecificServer (Request req, Response response) {
        String uuid = req.params(":guid");
        if (uuid != null && !uuid.isEmpty()) {
            CurseGUID guid = CurseGUID.deserialize(uuid);
            for (GroupNotification group : Main.getCacheService().getContacts().get().groups) {
                if (group.groupID.equals(guid)) {
                    if (group.isPublic && !group.hideNoAccess) {
                        Map map = Maps.newHashMap();
                        map.put("commonmark", Main.getCommonMarkUtils().renderToHTML(getMdForServer(group)));
                        map.put("titleText", group.groupTitle);
                        return map;
                    } else {
                        //TODO we need to toss a better error here!
                        return rendererror(req, response, uuid, 500);
                    }
                }
            }
        }
        //TODO we need to toss a better error here!
        return rendererror(req, response, uuid, 500);
    }

    public static String getMdForChannel (GroupNotification group, ChannelContract channel, boolean displayParent, boolean displayData) {
        StringBuilder builder = new StringBuilder();

        builder.append(CommonMarkUtils.h3(CommonMarkUtils.link(channel.groupTitle, "/channel/" + channel.groupID))).append(CommonMarkUtils.list("Public " + channel.isPublic))
                .append(CommonMarkUtils.list("hideNoAccess " + channel.hideNoAccess)).append(CommonMarkUtils.list("order " + channel.displayOrder))
                .append(CommonMarkUtils.list("displayCategory " + channel.displayCategory)).append(CommonMarkUtils.list("displayCategoryRank " + channel.displayCategoryRank))
                .append(CommonMarkUtils.list("Default Channel: " + channel.isDefaultChannel));
        if (channel.isPublic && channel.messageOfTheDay != null) {
            builder.append(CommonMarkUtils.list(channel.messageOfTheDay));
        }
        //TODO display enabled status alerts to the channel
        if (displayParent) {
            builder.append(CommonMarkUtils.h4("Parent Server " + CommonMarkUtils.link(group.groupTitle, "/server/" + group.groupID.serialize())));
        }
        if (displayData) {
            Optional<List<ModerationLog>> logs = MongoConnection.getModerationLogs(channel.groupID);
            if (logs.isPresent() && logs.get().size() > 0) {
                try {
                    builder.append(CommonMarkUtils.tableHeader("Date", "Type", "ActionPerformer", "MessageOwner", "Info"));
                    logs.get().stream().sorted((e1, e2) -> Long.compare(e1.getActionTime().getTime(), e2.getActionTime().getTime())).forEach(cnl -> {
                        builder.append(CommonMarkUtils.tableRow(cnl.getActionTime().toString(), cnl.getType(), cnl.getPerformerName(), cnl.getAffectsName(), cnl.getInfo()));
                    });
                } catch (Exception e) {
                    log.error("error with tables", e);
                }
            }
            //TODO display moderation data -- only in public listed for now
        }
        return builder.toString();
    }

    public static String getMdForGroup (GroupNotification group, boolean displayChannelData) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonMarkUtils.h2(CommonMarkUtils.link(group.groupTitle, "/server/" + group.groupID.serialize())))
                .append(CommonMarkUtils.list(group.messageOfTheDay == null ? "MOTD: " : group.messageOfTheDay))
                .append(CommonMarkUtils.list("Public " + group.isPublic))
                .append(CommonMarkUtils.list("hideNoAccess " + group.hideNoAccess)).append(CommonMarkUtils.list("displayOrder " + group.displayOrder));
        group.channels.stream().filter(a -> (a.isPublic && displayChannelData)).sorted((e1, e2) -> Integer.compare(e1.displayOrder, e2.displayOrder))
                .forEach(channel -> builder.append(getMdForChannel(group, channel, false, false)));
        return builder.toString();
    }

    public static String getMdForServer (GroupNotification group) {
        return getMdForGroup(group, true);
    }

    //TODO display in order
    //TODO cache this
    public static String getMdForServers () {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonMarkUtils.h1("Servers")).append("\n");
        int nps = 0;
        for (GroupNotification group : Main.getCacheService().getContacts().get().groups) {
            if (group.status == GroupStatus.NORMAL && group.isPublic && !group.hideNoAccess) {
                builder.append(getMdForGroup(group, false));
            }
            if (!group.isPublic && group.hideNoAccess) {
                nps++;
            }
            builder.append("\n\n");
        }
        builder.append(CommonMarkUtils.h4("Additional Info:")).append(CommonMarkUtils.list("In " + nps + " Hidden Servers "))
                .append(CommonMarkUtils.list("TODO: List channels in order & by folders"));
        return builder.toString();
    }

    private static Map rendererror (Request req, Response response, String uuid, int code) {
        Map map = Maps.newHashMap();
        map.put("commonmark", Main.getCommonMarkUtils().renderToHTML("ERROR " + uuid));
        map.put("titleText", "ERROR");
        response.status(code);
        return map;

    }
}
