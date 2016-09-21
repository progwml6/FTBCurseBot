package com.feed_the_beast.ftbcurseappbot.webserver.endpoints;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;

/**
 * this should be useful to anyone who wants to know what is being run
 */

public class HealthEndpoint {

    private final String buildNumber;
    private final String gitBranch;
    private final String gitCommit;
    private final String jenkinsJobName;
    private final String jenkinsTag;
    private final String userName;
    private final String version;

    public HealthEndpoint () {
        //most of these will be empty when not being provided by the build system for containers
        buildNumber = System.getenv("BUILD_NUMBER");
        gitBranch = System.getenv("GIT_BRANCH");
        gitCommit = System.getenv("GIT_COMMIT");
        jenkinsJobName = System.getenv("JOB_NAME");
        jenkinsTag = System.getenv("BUILD_TAG");
        userName = Config.getUsername();
        version = Main.VERSION;
    }

}
