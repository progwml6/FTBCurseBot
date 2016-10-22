package com.feed_the_beast.ftbcurseappbot;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class Config {
    @Getter
    private static CommentedConfigurationNode config = null;
    @Getter
    private static String botTrigger;
    @Getter
    private static Optional<List<String>> bBStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> cFStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> dynStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> mcStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> gHStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> travisStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> twitchStatusChangeNotificationsEnabled = Optional.empty();

    public static void load (File configFl) {
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(configFl).build(); // Create the loader
            config = loader.load(); // Load the configuration into memory
        } catch (IOException e) {
            log.error("error with config loading", e);
        }
        botTrigger = config.getNode("botSettings", "triggerKey").getString("!");
        try {
            bBStatusChangeNotificationsEnabled = Optional.ofNullable(config.getNode("botSettings", "BBStatusChangeNotificationsEnabled").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            log.error("couldn't map bot settings - bb", e);
        }
        try {
            cFStatusChangeNotificationsEnabled = Optional.ofNullable(config.getNode("botSettings", "CFStatusChangeNotificationsEnabled").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            log.error("couldn't map bot settings - cf", e);
        }
        try {
            dynStatusChangeNotificationsEnabled = Optional.ofNullable(config.getNode("botSettings", "DynStatusChangeNotificationsEnabled").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            log.error("couldn't map bot settings - dyn", e);
        }
        try {
            gHStatusChangeNotificationsEnabled = Optional.ofNullable(config.getNode("botSettings", "GHStatusChangeNotificationsEnabled").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            log.error("couldn't map bot settings - gh", e);
        }

        try {
            mcStatusChangeNotificationsEnabled = Optional.ofNullable(config.getNode("botSettings", "MCStatusChangeNotificationsEnabled").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            log.error("couldn't map bot settings - mc", e);
        }

        try {
            travisStatusChangeNotificationsEnabled = Optional.ofNullable(config.getNode("botSettings", "TravisStatusChangeNotificationsEnabled").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            log.error("couldn't map bot settings - travis", e);
        }
        try {
            twitchStatusChangeNotificationsEnabled = Optional.ofNullable(config.getNode("botSettings", "TwitchStatusChangeNotificationsEnabled").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            log.error("couldn't map bot settings - twitch", e);
        }

        log.info("bot trigger is " + botTrigger);

    }

    public static String getUsername () {
        return config.getNode("credentials", "CurseAppLogin", "username").getString();
    }

    public static String getPassword () {
        return config.getNode("credentials", "CurseAppLogin", "password").getString();
    }

    public static boolean isDebugEnabled () {
        return config.getNode("botSettings", "debug").getBoolean(false);
    }

    public static boolean isWebEnabled () {
        return config.getNode("botSettings", "webEnabled").getBoolean(true);
    }

    public static int getWebPort () {
        return config.getNode("botSettings", "webPort").getInt(4567);
    }
}

