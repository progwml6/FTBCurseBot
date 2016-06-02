package com.feed_the_beast.ftbcurseappbot.api.statuspageio;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

@Data
public class Page {
    private String id;
    private String name;
    private String url;
    @SerializedName("updated_at")
    private Date updatedAt;
}
