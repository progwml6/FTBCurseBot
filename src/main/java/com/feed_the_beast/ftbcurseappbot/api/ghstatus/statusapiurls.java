package com.feed_the_beast.ftbcurseappbot.api.ghstatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class StatusApiUrls {
    @Getter
    private String status_url;
    @Getter
    private String messages_url;
    @Getter
    private String last_message_url;
}
