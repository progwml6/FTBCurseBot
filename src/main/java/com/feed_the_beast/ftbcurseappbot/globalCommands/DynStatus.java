package com.feed_the_beast.ftbcurseappbot.globalCommands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynStatus extends StatusPageIOBase {
    private static final String bbStatusAPIBase = "https://www.dynstatus.com/api/";

    private static StatusCommandBase instance;

    public DynStatus () {
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
        return "dyn";
    }

    @Override
    public String getHelp () {
        return "gets dyn dns status";
    }

}
