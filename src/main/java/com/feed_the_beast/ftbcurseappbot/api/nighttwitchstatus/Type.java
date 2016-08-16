package com.feed_the_beast.ftbcurseappbot.api.nighttwitchstatus;

import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Created by progwml6 on 8/16/16.
 */
@EqualsAndHashCode
public class Type {
    public List<Alert> alerts;
    public List<Server> servers;
}
