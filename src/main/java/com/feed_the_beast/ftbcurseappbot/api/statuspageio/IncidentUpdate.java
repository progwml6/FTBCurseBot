package com.feed_the_beast.ftbcurseappbot.api.statuspageio;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

@Data
public class IncidentUpdate {
    private String body;
    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("displayed_at")
    private Date displayedAt;
    private String id;
    @SerializedName("incident_id")
    private String incidentId;
    private String status;
    @SerializedName("updated_at")
    private Date updatedAt;
}
