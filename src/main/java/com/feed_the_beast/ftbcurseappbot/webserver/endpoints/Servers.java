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

    public static String getMdForServers () {
        StringBuilder builder = new StringBuilder();
        builder.append(CommonMarkUtils.h1("Servers")).append("\n");
        for (GroupNotification group : Main.getCacheService().getContacts().get().groups) {
            if (group.status == GroupStatus.NORMAL) {//TODO public visible to outside only
                builder.append(CommonMarkUtils.h2(group.groupTitle)).append("\n").append(CommonMarkUtils.list(group.messageOfTheDay)).append(CommonMarkUtils.list("Public " + group.isPublic))
                        .append(CommonMarkUtils.list("hideNoAccess " + group.hideNoAccess));
                for (ChannelContract channel : group.channels) {
                    builder.append(CommonMarkUtils.h3(channel.groupTitle)).append(CommonMarkUtils.list("Public " + channel.isPublic))
                            .append(CommonMarkUtils.list("hideNoAccess " + channel.hideNoAccess));
                }
            }
            builder.append("\n\n");
        }
        return "";
    }
}
