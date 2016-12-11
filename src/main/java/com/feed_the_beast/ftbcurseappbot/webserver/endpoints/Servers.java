package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.utils.CommonMarkUtils;
import com.feed_the_beast.javacurselib.common.enums.GroupStatus;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ChannelContract;
import com.feed_the_beast.javacurselib.service.contacts.contacts.GroupNotification;
import com.google.common.collect.Maps;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by progwml6 on 12/10/16.
 */
public class Servers {
    public static Map render (@Nonnull String title) {
        Map map = Maps.newHashMap();
        map.put("commonmark", Main.getCommonMarkUtils().renderToHTML(getMdForServers()));
        map.put("titleText", title);
        return map;
    }

    //TODO display in order
    public static String getMdForServers () {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonMarkUtils.h1("Servers")).append("\n");
        int nps = 0, npc = 0;
        for (GroupNotification group : Main.getCacheService().getContacts().get().groups) {
            if (group.status == GroupStatus.NORMAL && group.isPublic && !group.hideNoAccess) {
                builder.append(CommonMarkUtils.h2(group.groupTitle)).append(CommonMarkUtils.list(group.messageOfTheDay == null ? "MOTD: " : group.messageOfTheDay))
                        .append(CommonMarkUtils.list("Public " + group.isPublic))
                        .append(CommonMarkUtils.list("hideNoAccess " + group.hideNoAccess));
                //group.displayOrder;
                for (ChannelContract channel : group.channels) {
                    if (!channel.hideNoAccess) {
                        builder.append(CommonMarkUtils.h3(channel.groupTitle)).append(CommonMarkUtils.list("Public " + channel.isPublic))
                                .append(CommonMarkUtils.list("hideNoAccess " + channel.hideNoAccess));
                        if (channel.isPublic && channel.messageOfTheDay != null) {
                            builder.append(CommonMarkUtils.list(channel.messageOfTheDay));
                        }
                        //channel.displayCategory;
                        //channel.displayCategoryRank;
                    } else {
                        npc++;
                    }
                }
                builder.append("\n\n");
            }
            if (!group.isPublic && group.hideNoAccess) {
                nps++;
            }
        }
        builder.append(CommonMarkUtils.h4("Additional Info:")).append(CommonMarkUtils.list("In " + nps + " Hidden Servers ")).append(CommonMarkUtils.list("In " + npc + " Hidden Channels "))
                .append(CommonMarkUtils.list("TODO: List channels in order & by folders"))
        return builder.toString();
    }
}
