package com.feed_the_beast.ftbcurseappbot.persistence.data;


import lombok.Data;

@Data
public class APIServer {
    private String serverName;
    private String channelName;
    private String channelID;
    private String serverID;

    public APIServer() {
        //for jackson
    }
    public APIServer(String serverName, String channelName, String serverID, String channelID) {
        this.serverName = serverName;
        this.channelName = channelName;
        this.serverID = serverID;
        this.channelID = channelID;
    }
}
