package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;

import com.feed_the_beast.ftbcurseappbot.Main;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Created by progwml6 on 7/16/16.
 */
public class Md {
    public static Route render = (Request request, Response response) -> {
        return Main.getCommonMarkUtils().renderToHTML("## Commonmark Parser > Web :D\n"
                + "### It will probably be used for help pages\n"
                + "This is rendered into html from commonmark inside FTBBot.  This is powered by\n"
                + "[Atlassian's Commonmark lib](https://github.com/atlassian/commonmark-java).\n"
                + "\n"
                + "1. nice list\n"
                + "2. right\n"
                + "   - sublist\n"
                + "   - sublist\n"
                + "\n");
    };
}