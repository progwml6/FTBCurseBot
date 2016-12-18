package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.google.common.collect.Maps;

import java.util.Map;

import javax.annotation.Nonnull;

public class Md {
    public static Map render (@Nonnull String text, @Nonnull String title) {
        Map map = Maps.newHashMap();
        map.put("commonmark", Main.getCommonMarkUtils().renderToHTML(text));
        map.put("titleText", title);
        return map;
    }

}