package com.feed_the_beast.ftbcurseappbot.persistence;

import lombok.Getter;

/**
 * Created by progwml6 on 6/10/16.
 */
public enum PersistanceEventType {
    BAN("BAN"), DELETE("DELETE"), IP_BAN("IP_BAN"), KICK("KICK"), PERM_BAN("PERM_BAN"), EDIT_MESSAGE("EDIT_MESSAGE");
    @Getter
    private String name;

    PersistanceEventType (String name) {
        this.name = name;
    }
}
