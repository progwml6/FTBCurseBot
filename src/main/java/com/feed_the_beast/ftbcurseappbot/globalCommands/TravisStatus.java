package com.feed_the_beast.ftbcurseappbot.globalCommands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TravisStatus extends StatusPageIOBase {
    private static final String travisStatusAPIBase = "https://www.traviscistatus.com/api/";

    private static StatusCommandBase instance;

    public TravisStatus () {
        instance = this;
    }

    public static StatusCommandBase getInstance () {
        return instance;
    }

    @Override
    public String getBaseURL () {
        return travisStatusAPIBase;
    }

    @Override
    public String getService () {
        return "travis";
    }

    @Override
    public String getHelp () {
        return "gets travis status";
    }

}
