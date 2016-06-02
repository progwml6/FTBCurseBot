package com.feed_the_beast.ftbcurseappbot.api.statuspageio;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ScheduledMaintenence {
    @SerializedName("created_at")
    private Date createdAt;
    private String id;
    private String impact;
    @SerializedName("incident_updates")
    private List<IncidentUpdate> incidentUpdates;
    //private String monitoring_at;
    private String name;
    @SerializedName("page_id")
    private String pageId;
    @SerializedName("resolved_at")
    private Date resolvedAt;
    @SerializedName("scheduled_for")
    private Date scheduledFor;
    @SerializedName("scheduled_until")
    private Date scheduledUntil;
    private String shortlink;
    private String status;
    @SerializedName("updated_at")
    private Date updatedAt;
}
