package com.feed_the_beast.ftbcurseappbot.persistence;

import com.feed_the_beast.javacurselib.common.enums.ConversationNotificationType;
import lombok.Getter;

public enum PersistanceEventType {
    BAN("BAN"), DELETE("DELETE"), IP_BAN("IP_BAN"), KICK("KICK"), PERM_BAN("PERM_BAN"), EDIT_MESSAGE("EDIT_MESSAGE"), LIKED("LIKED"), UNKNOWN("UNKNOWN"), NORMAL("NORMAL");
    @Getter
    private String name;

    PersistanceEventType (String name) {
        this.name = name;
    }

    public static PersistanceEventType getTypeFromConversationNotificationType (ConversationNotificationType type) {
        switch (type) {
        case DELETED:
            return DELETE;
        case LIKED:
            return LIKED;
        case NORMAL:
            return NORMAL;
        case EDITED:
            return EDIT_MESSAGE;
        default:
            return UNKNOWN;
        }
    }
}
