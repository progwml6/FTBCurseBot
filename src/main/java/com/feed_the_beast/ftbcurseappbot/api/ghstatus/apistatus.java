package com.feed_the_beast.ftbcurseappbot.api.ghstatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@EqualsAndHashCode
public class ApiStatus {
    @Getter
    private String status;
    @Getter
    private Date last_updated;
}
