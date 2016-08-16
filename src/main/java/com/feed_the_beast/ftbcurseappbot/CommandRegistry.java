package com.feed_the_beast.ftbcurseappbot;

import com.feed_the_beast.ftbcurseappbot.api.ICommandBase;
import com.feed_the_beast.ftbcurseappbot.globalCommands.BBStatus;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Ban;
import com.feed_the_beast.ftbcurseappbot.globalCommands.CFStatus;
import com.feed_the_beast.ftbcurseappbot.globalCommands.ChatFormatting;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Commands;
import com.feed_the_beast.ftbcurseappbot.globalCommands.CustomCommands;
import com.feed_the_beast.ftbcurseappbot.globalCommands.DelCmd;
import com.feed_the_beast.ftbcurseappbot.globalCommands.FTBBot;
import com.feed_the_beast.ftbcurseappbot.globalCommands.FTBFaq;
import com.feed_the_beast.ftbcurseappbot.globalCommands.GHStatus;
import com.feed_the_beast.ftbcurseappbot.globalCommands.HasPaidMC;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Help;
import com.feed_the_beast.ftbcurseappbot.globalCommands.IpBan;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Kick;
import com.feed_the_beast.ftbcurseappbot.globalCommands.MCDrama;
import com.feed_the_beast.ftbcurseappbot.globalCommands.MCStatus;
import com.feed_the_beast.ftbcurseappbot.globalCommands.MCUUID;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Repeat;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Setcmd;
import com.feed_the_beast.ftbcurseappbot.globalCommands.Shorten;
import com.feed_the_beast.ftbcurseappbot.globalCommands.TravisStatus;
import com.feed_the_beast.ftbcurseappbot.globalCommands.TwitchStatus;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.persistence.data.MongoCommand;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Created by progwml6 on 5/20/16.
 */
@Slf4j
public class CommandRegistry {

    @Getter
    private static HashMap<Pattern, ICommandBase> commands = new HashMap<>();

    public static void register (ICommandBase c) {
        commands.put(c.getTriggerRegex(), c);
    }

    public static boolean doesCommandExist (String channel, String command) {
        for (Pattern p : commands.keySet()) {
            if (p.matcher(command).matches()) {
                return true;
            }
        }
        //TODO channel/server only commands
        return false;
    }

    public static Optional<ICommandBase> getCommand (@Nonnull CurseGUID server, @Nonnull String s) {
        for (Map.Entry<Pattern, ICommandBase> me : commands.entrySet()) {
            if (me.getKey().matcher(s).matches()) {
                return Optional.of(me.getValue());
            }
        }
        //TODO channel only custom commands
        return Optional.empty();
    }

    /**
     * process server custom commands
     * use rootConversationID from conversations this is the real serverId
     * @param server server
     * @param webSocket websocket instance
     * @param msg converstation message
     * @return true if command was executed, false otherwise
     */
    public static boolean processServerCommands (@Nonnull CurseGUID server, @Nonnull WebSocket webSocket, @Nonnull ConversationMessageNotification msg) {
        Optional<List<MongoCommand>> commands = Main.getCacheService().getCustomCommandsForServer(server);
        if (!commands.isPresent()) {
            log.info("no commands present for server {} checking cache", msg.rootConversationID);
            commands = MongoConnection.getCommandsForServer(server);
            if (commands.isPresent()) {
                Main.getCacheService().setServerCommandsEntry(server, commands.get());
            }
        }
        if (commands.isPresent()) {
            for (MongoCommand c : commands.get()) {
                if (c.getPattern().matcher(msg.body).matches()) {
                    return CustomCommandExecutor.execute(c, webSocket, msg);
                }
            }

        }
        return false;

    }

    private static boolean regexMatch (String s, String regex) {
        return Pattern.matches(s, regex);
    }
    //TODO add unregister command

    public static void registerBaseCommands () {
        log.info("registering base commands");
        new Ban();
        new BBStatus();
        new CFStatus();
        new ChatFormatting();
        new Commands();
        new CustomCommands();
        new DelCmd();
        new FTBBot();
        new FTBFaq();
        new GHStatus();
        new Help();
        new HasPaidMC();
        new IpBan();
        new Kick();
        new MCDrama();
        new MCStatus();
        new MCUUID();
        new Repeat();
        new Setcmd();
        new Shorten();
        new TravisStatus();
        new TwitchStatus();
        log.info("registered " + commands.size() + " base commands");
    }
}
