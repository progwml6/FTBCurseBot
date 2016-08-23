package com.feed_the_beast.ftbcurseappbot.globalCommands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BBStatus extends StatusPageIOBase {
    private static final String bbStatusAPIBase = "http://status.bitbucket.org/api/";

    private static StatusCommandBase instance;

    public BBStatus () {
        instance = this;
    }

    public static StatusCommandBase getInstance () {
        return instance;
    }

    @Override
    public String getBaseURL () {
        return bbStatusAPIBase;
    }

    @Override
    public String getService () {
        return "bb";
    }

    @Override
    public String getHelp () {
        return "gets Bitbucket status";
    }

}
