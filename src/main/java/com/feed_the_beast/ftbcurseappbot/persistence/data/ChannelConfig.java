package com.feed_the_beast.ftbcurseappbot.persistence.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoId;

import java.util.List;

public class ChannelConfig {
    @Getter
    @MongoId
    private String channelID;
    @Getter
    @MongoId
    private String serverID;
    @Getter
    @Setter
    private Boolean isPublic;
    @Getter
    @Setter
    private Boolean hideNoAccess;
    @Getter
    @Setter
    private List<String> enabledAlerts;

    public ChannelConfig () {
        //needed for Jackson
    }

    @JsonIgnore
    public boolean canDisplayDataOnWeb () {
        return isPublic;
    }

    @JsonIgnore
    public boolean canDisplayChannelOnWeb () {
        return isPublic && !hideNoAccess;
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
