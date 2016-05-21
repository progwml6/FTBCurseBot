package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.ftbcurseappbot.api.ICommandBase;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Commands;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Help;
import com.feed_the_beast.ftbcurseappbot.globalCommands.MCStatus;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Repeat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by progwml6 on 5/20/16.
 */
@Slf4j
public class CommandRegistry {

    @Getter
    public static HashMap<Pattern, ICommandBase> commands = new HashMap<>();

    public static void register (ICommandBase c) {
        commands.put(c.getTriggerRegex(), c);
    }

    public static boolean doesCommandExist (String channel, String command) {
        for (Pattern p : commands.keySet()) {
            if(p.matcher(command).matches())
                return true;
        }
        //TODO channel/server only commands
        return false;
    }

    public static Optional<ICommandBase> getCommand (String channel, String s) {
        for (Map.Entry<Pattern, ICommandBase> me: commands.entrySet()) {
            if(me.getKey().matcher(s).matches()) {
                return Optional.of(me.getValue());
            }
        }
        //TODO channel/server only custom commands
        return Optional.empty();
    }

    private static boolean regexMatch (String s, String regex) {
        return Pattern.matches(s, regex);
    }
    //TODO add unregister command

    public static void registerBaseCommands() {
        log.info("registering base commands");
        new Commands();
        new Help();
        new MCStatus();
        new Repeat();
        log.info("registered " + commands.size() + " base commands");
    }
}