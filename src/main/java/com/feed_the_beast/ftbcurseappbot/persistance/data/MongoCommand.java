package com.feed_the_beast.ftbcurseappbot.persistance.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.utils.EnumSetHelpers;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by progwml6 on 7/2/16.
 */
public class MongoCommand {
    @Getter
    @Setter
    private String regex;

    @Setter
    @Getter
    private String serverID;

    @Setter
    @Getter
    private boolean usesTrigger;

    @Setter
    @Getter
    private String content;

    @Setter
    @Getter
    private long permissions;

    @MongoObjectId
    @Getter
    @Setter
    private ObjectId _id;

    //TODO make sure this isn't serialized to mongo
    @JsonIgnore
    public CurseGUID getServerIDAsGUID () {
        return CurseGUID.newFromString(serverID);
    }

    public MongoCommand () {
        //needed for Jackson
    }

    public MongoCommand (@Nonnull String regex, @Nonnull String content, @Nullable Set<GroupPermissions> requiredPermissions, @Nonnull CurseGUID serverID, boolean usesTrigger) {
        this.regex = regex;
        this.content = content;
        this.serverID = serverID.serialize();
        this.usesTrigger = usesTrigger;
        this.permissions = EnumSetHelpers.serialize(requiredPermissions, GroupPermissions.class);
    }

}
