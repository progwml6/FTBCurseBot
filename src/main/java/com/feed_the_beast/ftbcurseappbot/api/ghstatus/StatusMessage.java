package com.feed_the_beast.ftbcurseappbot.api.ghstatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@EqualsAndHashCode
public class StatusMessage {
    @Getter
    private String status;
    @Getter
    private String body;
    @Getter
    private Date created_on;

}
