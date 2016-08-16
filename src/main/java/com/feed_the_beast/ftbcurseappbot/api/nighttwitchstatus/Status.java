package com.feed_the_beast.ftbcurseappbot.api.nighttwitchstatus;

import lombok.EqualsAndHashCode;

/**
 * Created by progwml6 on 8/16/16.
 */
@EqualsAndHashCode
public class Status {
    public Type web;
    public Type ingest;
    public Type chat;
}
