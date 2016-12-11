package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.utils.CommonMarkUtils;
import com.feed_the_beast.javacurselib.common.enums.GroupStatus;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ChannelContract;
import com.feed_the_beast.javacurselib.service.contacts.contacts.GroupNotification;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.google.common.collect.Maps;
import spark.Request;
import spark.Response;

import java.util.Map;

import javax.annotation.Nonnull;

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
                .append(CommonMarkUtils.list("hideNoAccess " + channel.hideNoAccess));
        if (channel.isPublic && channel.messageOfTheDay != null) {
            builder.append(CommonMarkUtils.list(channel.messageOfTheDay));
        }
        if (displayParent) {
            builder.append(CommonMarkUtils.h4("Parent Server " + CommonMarkUtils.link(group.groupTitle, "/server/" + group.groupID.serialize())));
        }
        if(displayData) {
            //TODO display moderation data -- only in public listed for now
        }
        //channel.displayCategory;
        //channel.displayCategoryRank;
        return builder.toString();
    }

    public static String getMdForGroup (GroupNotification group) {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonMarkUtils.h2(CommonMarkUtils.link(group.groupTitle, "/server/" + group.groupID.serialize())))
                .append(CommonMarkUtils.list(group.messageOfTheDay == null ? "MOTD: " : group.messageOfTheDay))
                .append(CommonMarkUtils.list("Public " + group.isPublic))
                .append(CommonMarkUtils.list("hideNoAccess " + group.hideNoAccess));
        //group.displayOrder;
        for (ChannelContract channel : group.channels) {
            if (!channel.hideNoAccess) {
                builder.append(getMdForChannel(group, channel, false, false));
            }
        }
        return builder.toString();
    }

    public static String getMdForServer (GroupNotification group) {
        StringBuilder builder = new StringBuilder();
        builder.append(getMdForGroup(group));
        return builder.toString();
    }

    //TODO display in order
    //TODO cache this
    public static String getMdForServers () {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonMarkUtils.h1("Servers")).append("\n");
        int nps = 0;
        for (GroupNotification group : Main.getCacheService().getContacts().get().groups) {
            if (group.status == GroupStatus.NORMAL && group.isPublic && !group.hideNoAccess) {
                builder.append(getMdForGroup(group));
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
