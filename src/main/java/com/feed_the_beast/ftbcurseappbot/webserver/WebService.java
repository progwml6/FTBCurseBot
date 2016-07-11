package com.feed_the_beast.ftbcurseappbot.webserver;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.debug.DebugScreen.enableDebugScreen;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.webserver.endpoints.HealthEndpoint;
import com.feed_the_beast.ftbcurseappbot.webserver.transformers.JsonTransformer;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.HmacUtils;

import javax.annotation.Nonnull;

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
            String key = "NEED_SECURE_KEYS_AND_STORAGE_CODE";//TODO<<
            if (!Strings.isNullOrEmpty(signature) && ghSignatureMatches(signature, request.body(), key)) {
                if (!handleGHWebhook(eventType, request.body())) {
                    //TODO throw 500 error here
                }
            } else {
                //TODO throw credential error
            }
            return API_POST_SUCCESS;
        }, new JsonTransformer());

        get("/", (request, response) -> "No fancy web config yet check back soon!");

    }

    private boolean ghSignatureMatches (@Nonnull String signature, @Nonnull String body, @Nonnull String key) {
        return HmacUtils.hmacSha1Hex(key, body).equals(signature);
    }

    private boolean handleGHWebhook (String eventtype, String body) {
        switch (eventtype) {
        case "commit_comment":
            break;
        case "create":
            break;
        case "delete":
            break;
        case "deployment":
            break;
        case "deployment_status":
            break;
        case "fork":
            break;
        case "gollum":
            break;
        case "issue":
            break;
        case "issue_comment":
            break;
        case "issues":
            break;
        case "member":
            break;
        case "membership":
            break;
        case "page_build":
            break;
        case "public":
            break;
        case "pull_request_review_comment":
            break;
        case "pull_request":
            break;
        case "push":
            break;
        case "repository":
            break;
        case "release":
            break;
        case "status":
            break;
        case "team_add":
            break;
        case "watch":
            break;
        case "*":
            break; //wildcard
        case "ping":
            break; //tests
        default:
            //TODO implement
            break;
        }
        return true;
    }
}
