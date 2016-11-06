package com.feed_the_beast.ftbcurseappbot.persistence.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MongoCurseforgeCheck {
    @MongoObjectId
    @Getter
    @Setter
    private ObjectId _id;
    @Setter
    @Getter
    private String serverID;
    @Setter
    @Getter
    private String channelID;
    @Setter
    @Getter
    private String author;
    @Getter
    @Setter
    //optional
    private String type;

    public MongoCurseforgeCheck (@Nonnull String author, @Nullable String type, @Nonnull CurseGUID serverID, @Nonnull CurseGUID channelID) {
        this.author = author;
        this.type = type;
        this.serverID = serverID.serialize();
        this.channelID = channelID.serialize();
    }

    public MongoCurseforgeCheck (@Nonnull String author, @Nonnull CurseGUID serverID, @Nonnull CurseGUID channelID) {
        this.author = author;
        this.type = type;
        this.serverID = serverID.serialize();
        this.channelID = channelID.serialize();
    }

    public MongoCurseforgeCheck () {
        //added for serialization
    }

    @JsonIgnore
    public CurseGUID getServerIDAsGUID () {
        return CurseGUID.newInstance(serverID);
    }

    @JsonIgnore
    public CurseGUID getChannelIDAsGUID () {
        return CurseGUID.newInstance(channelID);
    }

}
