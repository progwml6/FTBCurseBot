package com.feed_the_beast.ftbcurseappbot.persistence.data;

import lombok.Getter;
import lombok.Setter;
import org.jongo.marshall.jackson.oid.MongoId;

public class VersionInfo {
    @Getter
    @MongoId
    private String service = "ftbcursebot";
    /**
     * version of the database should start at 0 and increase when migrations are needed
     */
    @Getter
    @Setter
    private int version = 3;
}
