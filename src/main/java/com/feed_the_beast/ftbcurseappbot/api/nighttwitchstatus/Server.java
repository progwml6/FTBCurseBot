package com.feed_the_beast.ftbcurseappbot.api.nighttwitchstatus;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Server {
    public String server;
    public String host;
    public boolean secure;
    public int port;
    public String description;
    public String status;
    public int loadTime;
    public String cluster;//chat only
    public String ip; //chat only
    public String protocol; //chat only
    public int errors; //chat only
    public int lag; //chat only
    public int[] pings;//chat only
}
