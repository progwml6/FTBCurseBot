package com.feed_the_beast.ftbcurseappbot.globalCommands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CFStatus extends StatusPageIOBase {

    private static final String cfStatusAPIBase = "https://www.cloudflarestatus.com/api/";

    public static StatusCommandBase instance;

    public CFStatus () {
        instance = this;
    }

    public static StatusCommandBase getInstance(){
        return instance;
    }

    @Override
    public String getBaseURL () {
        return cfStatusAPIBase;
    }


    @Override
    public String getService () {
        return "cf";
    }

    @Override
    public String getHelp () {
        return "gets Cloudflare status";
    }

}
