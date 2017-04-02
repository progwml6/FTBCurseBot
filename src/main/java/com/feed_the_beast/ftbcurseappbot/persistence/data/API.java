package com.feed_the_beast.ftbcurseappbot.persistence.data;

import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoId;

import java.util.List;

public class API {
    @Getter
    @Setter
    @MongoId
    private String apikey;

    @Getter
    @Setter
    private List<APIServer> channels;

    public API(String key, List<APIServer> channels) {
        this.apikey = key;
        this.channels = channels;

    }
}

