package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;

import static spark.Spark.halt;

import com.feed_the_beast.ftbcurseappbot.webserver.WebService;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.HmacUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.annotation.Nonnull;

/**
 * Created by progwml6 on 7/16/16.
 */
public class GithubWebhook {
    private static final String EVENT_HEADER = "X-GitHub-Event";
    private static final String SIGNATURE_HEADER = "X-Hub-Signature";
    private static final String DELIVERY_HEADER = "X-GitHub-Delivery";

    public static Route hook = (Request request, Response response) -> {
        if(!request.userAgent().startsWith("GitHub-Hookshot/")) {
            //TODO throw security error
        }
        String signature = request.headers(SIGNATURE_HEADER);
        String eventType = request.headers(EVENT_HEADER);
        String deliveryID = request.headers(DELIVERY_HEADER);
        String hookName = request.params(":hookName");
        String key = getKeyFromHookName(hookName);
        if (Strings.isNullOrEmpty(key)) {
            //TODO throw error here as we don't have a GH hook at this location
            halt(500, "internal server error");//TODO helper methods for errors
        }
        if (!Strings.isNullOrEmpty(signature) && ghSignatureMatches(signature, request.body(), key)) {
            if (!handleGHWebhook(eventType, request.body())) {
                //TODO throw 500 error here
                halt(500, "internal server error");//TODO helper methods for errors
            }
        } else {
            //TODO throw credential error
        }
        return WebService.API_POST_SUCCESS;
    };

    private static String getKeyFromHookName (String hookName) {
        return "TODO implement key storage/lookup/caching!!!";
    }

    private static boolean ghSignatureMatches (@Nonnull String signature, @Nonnull String body, @Nonnull String key) {
        return HmacUtils.hmacSha1Hex(key, body).equals(signature);
    }

    private static boolean handleGHWebhook (String eventtype, String body) {
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
