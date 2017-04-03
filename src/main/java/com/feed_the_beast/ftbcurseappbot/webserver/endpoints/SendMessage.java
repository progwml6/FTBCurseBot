package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;


import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.data.API;
import com.feed_the_beast.ftbcurseappbot.persistence.data.APIServer;
import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import com.feed_the_beast.ftbcurseappbot.webserver.WebService;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.google.common.base.Strings;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Optional;

import static spark.Spark.halt;

public class SendMessage {
    private static final String API_KEY_HEADER = "X-FTBBOT-KEY";

    public static Route send = (Request request, Response response) -> {
        String keyheader = request.headers(API_KEY_HEADER);
        if (!Strings.isNullOrEmpty(keyheader) && !keyheader.contains("'") && !keyheader.contains(",") && !keyheader.contains(":")) {//we don't want hidden commands getting ran
            if (MongoConnection.isPersistanceEnabled()) {
                Optional<API> data = MongoConnection.getAPIData(keyheader);
                if(data.isPresent()) {
                    MessageData md = JsonFactory.GSON.fromJson(request.body(), MessageData.class);
                    for(APIServer s : data.get().getChannels()) {
                        if(s.getServerName().equalsIgnoreCase(md.serverName) && s.getChannelName().equalsIgnoreCase(md.channelName)) {
                            Main.sendMessage(CurseGUID.newInstance(s.getChannelID()), md.message);
                            return WebService.API_POST_SUCCESS;
                        }
                    }
                }
            }
        } else {
            halt(500, "Internal Server Error, keyzzzz");//TODO put better error here
        }
        return WebService.API_POST_BUST;
    };
}
