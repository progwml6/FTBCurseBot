package com.feed_the_beast.ftbcurseappbot.utils;

import com.feed_the_beast.javacurselib.utils.DateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

/**
 * simple gson setup for most normal apis.
 */
public class JsonFactory {
    public static final Gson GSON;
    public static final boolean DEBUG = true;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.enableComplexMapKeySerialization();
        if (DEBUG) {
            builder.setPrettyPrinting();
        }
        GSON = builder.create();
    }

}
