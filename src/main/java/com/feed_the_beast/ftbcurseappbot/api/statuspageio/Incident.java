package com.feed_the_beast.ftbcurseappbot.api.statuspageio;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by progwml6 on 6/1/16.
 */
public class Incident {
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
    private String shortlink;
    private String status;
    @SerializedName("updated_at")
    private Date updatedAt;

}
