package com.feed_the_beast.ftbcurseappbot.webserver;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.debug.DebugScreen.enableDebugScreen;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.GithubWebhook;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.HealthEndpoint;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.Md;
import com.feed_the_beast.ftbcurseappbot.webserver.transformers.JsonTransformer;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Created by progwml6 on 6/21/16.
 */
public class WebService {

    public static final String API_POST_SUCCESS = "POST SUCCESSFUL";

    public WebService () {
        port(Main.getConfig().getNode("botSettings", "webPort").getInt(4567));
        if (Main.isDebug()) {
            enableDebugScreen();
        }
        get("/health", "application/json", (request, response) -> {
            return new HealthEndpoint();
        }, new JsonTransformer());
        get("/mdtest", (request, response) -> new ModelAndView(Md.render(), "commonmark.mustache"), new MustacheTemplateEngine());
        post("/webhooks/github/:hookName", GithubWebhook.hook);
        get("/", (request, response) -> "No fancy web config yet check back soon!");
    }

}
