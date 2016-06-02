package com.feed_the_beast.ftbcurseappbot.api.statuspageio;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

@Data
public class Component {
    @SerializedName("created_at")
    private Date createdAt;
    private String description;
    private String id;
    private String name;
    @SerializedName("page_id")
    private String pageId;
    private int position;
    private String status;
    @SerializedName("updated_at")
    private Date updatedAt;

}

