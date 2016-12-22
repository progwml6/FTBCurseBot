package com.feed_the_beast.ftbcurseappbot.persistence.data;

import lombok.Data;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;

@Data
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
    private Date messageTime;
    private Date actionTime;

    public ModerationLog () {
        //json serialization needs constructor
    }

    public ModerationLog (String type, String serverID, long performer, String performerName, long affects, String affectsName, String info, boolean doneByBot, Date actionTime, Date messageTime) {
        this.type = type;
        this.serverID = serverID;
        this.performer = performer;
        this.performerName = performerName;
        this.affects = affects;
        this.affectsName = affectsName;
        this.info = info;
        this.doneByBot = doneByBot;
        this.actionTime = actionTime;
        this.messageTime = messageTime;
    }
}
