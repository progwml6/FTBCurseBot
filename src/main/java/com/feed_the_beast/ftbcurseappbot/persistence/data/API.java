package com.feed_the_beast.ftbcurseappbot.persistence.data;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.List;

public class API {
    @Getter
    @Setter
    @MongoObjectId
    private ObjectId _id;

    @Getter
    @Setter
    private String apikey;

    @Getter
    @Setter
    private List<APIServer> channels;

    public API(String key, List<APIServer> channels) {
        this.apikey = key;
        this.channels = channels;

    }
}

