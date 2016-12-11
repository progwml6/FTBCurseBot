package com.feed_the_beast.ftbcurseappbot.persistence.data;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;

@Data
@Builder
public class ModerationLog {
    @MongoObjectId
    private ObjectId _id;

    private String type;
    private String serverName;
    private String serverID;
    private String channelID;
    private String channelName;
    private long performer;
    private String performerName;
    private long affects;
    private String affectsName;
    private String info;
    private Boolean doneByBot;
    private Date actionTime;

    public ModerationLog () {
        //json serialization needs constructor
    }
}
