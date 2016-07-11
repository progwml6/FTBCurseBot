package com.feed_the_beast.ftbcurseappbot.webserver;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.debug.DebugScreen.enableDebugScreen;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.HealthEndpoint;
import com.feed_the_beast.ftbcurseappbot.webserver.transformers.JsonTransformer;
import com.google.common.base.Strings;

/**
 * Created by progwml6 on 6/21/16.
 */
public class WebService {
    private static String API_POST_SUCCESS = "POST SUCCESSFUL";
    private static String EVENT_HEADER = "X-GitHub-Event";
    private static String SIGNATURE_HEADER = "X-Hub-Signature";
    private static String DELIVERY_HEADER = "X-GitHub-Delivery";

    public WebService () {
        port(Main.getConfig().getNode("botSettings", "webPort").getInt(4567));
        if (Main.isDebug()) {
            enableDebugScreen();
        }
        get("/health", "application/json", (request, response) -> {
            return new HealthEndpoint();
        }, new JsonTransformer());
        post("/webhooks/github", (request, response) -> {//TODO put in support for multiple repo hook locations
            String signature = request.headers(SIGNATURE_HEADER);
            String eventType = request.headers(EVENT_HEADER);
            String deliveryID = request.headers(DELIVERY_HEADER);
            if (!Strings.isNullOrEmpty(signature) && ghSignatureMatches(signature, request.body())) {
                handleGHWebhook(eventType, request.body());
                //TODO throw 500 error if this fails
            } else {
                //TODO throw credential error
            }
            return API_POST_SUCCESS;
        }, new JsonTransformer());

        get("/", (request, response) -> "No fancy web config yet check back soon!");

    }

    private boolean ghSignatureMatches (String signature, String body) {
        return true;
    }

    private boolean handleGHWebhook (String eventtype, String body) {
        return true;
    }
}
