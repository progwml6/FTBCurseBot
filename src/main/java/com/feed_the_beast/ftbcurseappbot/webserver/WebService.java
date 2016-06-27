package com.feed_the_beast.ftbcurseappbot.webserver;

import static spark.Spark.*;
import static spark.debug.DebugScreen.enableDebugScreen;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.HealthEndpoint;
import com.feed_the_beast.ftbcurseappbot.webserver.transformers.JsonTransformer;

/**
 * Created by progwml6 on 6/21/16.
 */
public class WebService {
    public WebService () {
        port(Main.getConfig().getNode("botSettings", "webPort").getInt(4567));
        if (Main.isDebug()) {
            enableDebugScreen();
        }
        get("/health", "application/json", (request, response) -> {
            return new HealthEndpoint();
        }, new JsonTransformer());
        get("/", (request, response) -> "No fancy web config yet check back soon!");

    }
}
