package com.feed_the_beast.ftbcurseappbot.webserver.transformers;

import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import spark.ResponseTransformer;

/**
 * render json attributes
 */
public class JsonTransformer implements ResponseTransformer {


    @Override
    public String render(Object model) {
        return JsonFactory.GSON.toJson(model);
    }
}
