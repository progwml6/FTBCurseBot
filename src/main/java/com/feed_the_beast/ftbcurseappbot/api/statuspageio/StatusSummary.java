package com.feed_the_beast.ftbcurseappbot.api.statuspageio;

import java.util.List;

/**
 * Created by progwml6 on 6/1/16.
 */
public class StatusSummary {
    public Page page;
    public List<Component> components;
    public List<Incident> incidents;
    public List<ScheduledMaintenence> scheduled_maintenances;
    public Status status;
}
