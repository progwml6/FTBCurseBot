package com.feed_the_beast.ftbcurseappbot.webserver;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.debug.DebugScreen.enableDebugScreen;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.utils.CommonMarkUtils;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.GithubWebhook;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.HealthEndpoint;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.Md;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.Servers;
import com.feed_the_beast.ftbcurseappbot.webserver.transformers.JsonTransformer;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Created by progwml6 on 6/21/16.
 */
public class WebService {

    public static final String API_POST_SUCCESS = "POST SUCCESSFUL";
    private static final String COMMONMARK_TEMPLATE = "commonmark.mustache";
    public WebService () {
        if (Config.getProxyPath().isPresent()) {
            CommonMarkUtils.setProxypath(Config.getProxyPath().get());
        }
        port(Config.getWebPort());
        if (Config.isDebugEnabled()) {
            enableDebugScreen();
        }
        get("/health", "application/json", (request, response) -> new HealthEndpoint(), new JsonTransformer());
        String cmtest = "## Commonmark Parser > Web :D\n"
                + "### It will probably be used for help pages\n"
                + "This is rendered into html from commonmark inside FTBBot.  This is powered by\n"
                + "[Atlassian's Commonmark lib](https://github.com/atlassian/commonmark-java).\n"
                + "\n"
                + "1. nice list\n"
                + "2. right\n"
                + "   - sublist\n"
                + "   - sublist\n"
                + "\n";
        //TODO this needs to be able to take a ?raw=true parameter or something for API stuff
        get("/mdtest", (request, response) -> new ModelAndView(Md.render(cmtest, "CM Test Title"), COMMONMARK_TEMPLATE), new MustacheTemplateEngine());
        get("/servers", (request, response) -> new ModelAndView(Servers.render(Config.getUsername() + " Servers"), COMMONMARK_TEMPLATE), new MustacheTemplateEngine());
        get("/server/:guid", (request, response) -> new ModelAndView(Servers.renderSpecificServer(request, response), COMMONMARK_TEMPLATE), new MustacheTemplateEngine());
        get("/channel/:guid", (request, response) -> new ModelAndView(Servers.renderSpecificChannel(request, response), COMMONMARK_TEMPLATE), new MustacheTemplateEngine());
        post("/webhooks/github/:hookName", GithubWebhook.hook);
        get("/", (request, response) -> new ModelAndView(Md.render(CommonMarkUtils.link("Servers list/moderation data", "/servers"), "Bot Home"), COMMONMARK_TEMPLATE), new MustacheTemplateEngine());
    }

}
