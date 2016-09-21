package com.feed_the_beast.ftbcurseappbot.persistence.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.utils.EnumSetHelpers;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Set;
import java.util.regex.Pattern;

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

    public MongoCommand () {
        //needed for Jackson
    }

    //TODO channel level, folder level, multi channel commands, role based stuff
    public MongoCommand (@Nonnull String regex, @Nonnull String content, @Nullable Set<GroupPermissions> requiredPermissions, @Nonnull CurseGUID serverID, boolean usesTrigger) {
        this.regex = regex;
        this.content = content;
        this.serverID = serverID.serialize();
        this.usesTrigger = usesTrigger;
        this.permissions = EnumSetHelpers.serialize(requiredPermissions, GroupPermissions.class);
    }

    //TODO make sure this isn't serialized to mongo
    @JsonIgnore
    public CurseGUID getServerIDAsGUID () {
        return CurseGUID.newInstance(serverID);
    }

    //TODO make sure this isn't serialized to mongo
    @JsonIgnore
    public Pattern getPattern () {
        if (usesTrigger) {//if it uses triggers follow same rules as simple commands
            return Pattern.compile("(?m)^" + Config.getBotTrigger() + regex + "(.*)", Pattern.CASE_INSENSITIVE);
        } else {
            return Pattern.compile(regex);
        }
    }

    @JsonIgnore
    public Set<GroupPermissions> getGroupPermissions () {
        return EnumSetHelpers.deserialize(permissions, GroupPermissions.class);
    }

}
