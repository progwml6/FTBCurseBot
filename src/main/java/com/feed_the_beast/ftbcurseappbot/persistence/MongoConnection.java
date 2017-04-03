package com.feed_the_beast.ftbcurseappbot.persistence;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistence.data.*;
import com.feed_the_beast.javacurselib.common.enums.GroupPermissions;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.utils.EnumSetHelpers;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.jongo.Jongo;
import org.jongo.MongoCursor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Slf4j
public class MongoConnection {
    @Getter
    private static boolean persistanceEnabled = false;
    @Getter
    private static MongoClient client;
    @Getter
    private static MongoDatabase database;
    @Getter
    private static Jongo jongo;
    private static String MONGO_CONFIG_COLLECTION = "dbinfo";
    private static String MONGO_COMMANDS_COLLECTION = "commands";
    private static String MONGO_MODERATION_LOGGING_COLLECTION = "moderation_logs";
    private static String MONGO_SERVER_CONFIG_COLLECTION = "serverconfigs";
    private static String MONGO_CHANNEL_CONFIG_COLLECTION = "channelconfigs";
    private static String MONGO_CURSECHECKS_COLLECTION = "cursechecks";
    private static String MONGO_API_COLLECTION = "api";

    public static void start() {
        CommentedConfigurationNode config = Config.getConfig().getNode("mongo");
        if (config.getNode("enabled").getBoolean()) {
            persistanceEnabled = true;
            log.info("starting up mongo client");
            String username = config.getNode("username").getString();
            String pass = config.getNode("password").getString();
            String url = config.getNode("url").getString();
            String db = config.getNode("database").getString();

            int port = config.getNode("port").getInt(27017);
            if (username != null && !username.isEmpty() && pass != null && !pass.isEmpty()) {
                MongoCredential credential = MongoCredential.createCredential(username, db, pass.toCharArray());
                client = new MongoClient(new ServerAddress(url, port), Collections.singletonList(credential));
            } else {
                client = new MongoClient(url, port);
            }
            database = client.getDatabase(db);
            jongo = new Jongo(client.getDB(db));
            log.info("started up mongo client");
            VersionInfo info = jongo.getCollection(MONGO_CONFIG_COLLECTION).findOne("{_id: 'ftbcursebot'}").as(VersionInfo.class);
            if (info == null) {
                info = new VersionInfo();
                info.setVersion(0);//we need to force a migration to occur this will get saved in the migration code
                log.info("created VersionInfo for database");
            } else {
                log.info("mongo DB version: " + info.getVersion());
            }
            VersionInfo current = new VersionInfo();
            if (info.getVersion() < current.getVersion()) {
                log.info("database needs to be migrated from version {} to {}", info.getVersion(), current.getVersion());
                migrate(info, current);
            }
        }
    }

    public static void logEvent(PersistanceEventType event, CurseGUID serverID, @Nullable CurseGUID channel, long performer, String performerName, long affects, String nameAffects, String info,
                                boolean wasDoneByBot, long actionTime, long messageTime) {
        Date orig = null;
        if (messageTime > 0) {
            orig = new Date(messageTime);
        }

        if (StringUtils.isEmpty(info)) {//put the original messages from deletions, etc. in the database for display later
            Optional<List<ModerationLog>> lgs = Optional.empty();
            if (orig != null) {
                lgs = getMessageLog(channel, orig);
            }
            if (orig == null || !lgs.isPresent()) {
                lgs = getMessageLog(channel, new Date(actionTime));
            }
            if (lgs.isPresent()) {
                info = lgs.get().get(0).getInfo();
            }
        }
        ModerationLog log = new ModerationLog(event.getName(), serverID.serialize(), performer, performerName, affects, nameAffects,
                info, wasDoneByBot, new Date(actionTime), orig);
        Optional<String> gpn = Main.getCacheService().getContacts().get().getGroupNamebyId(serverID);
        gpn.ifPresent(log::setServerName);
        if (channel != null) {
            log.setChannelID(channel.serialize());
            Optional<String> chn = Main.getCacheService().getContacts().get().getChannelNamebyId(channel);
            chn.ifPresent(log::setChannelName);
        }
        jongo.getCollection(MONGO_MODERATION_LOGGING_COLLECTION).save(log);

    }

    /**
     * @param regex               command regex
     * @param content             content of the command when the regex matches
     * @param requiredPermissions required permissions(none when null)
     * @param serverID            curse server ID
     * @param usesTrigger         uses the bot's trigger if true like simple commands, if false this is a regex based command
     */
    public static void createOrModifyCommandForServer(@Nonnull String regex, @Nonnull String content, @Nullable Set<GroupPermissions> requiredPermissions, @Nonnull CurseGUID serverID,
                                                      boolean usesTrigger) {
        Set<GroupPermissions> commandPermissions = requiredPermissions;
        if (commandPermissions == null || commandPermissions.isEmpty()) {
            commandPermissions = GroupPermissions.NONE;
        }
        MongoCommand command = jongo.getCollection(MONGO_COMMANDS_COLLECTION).findOne("{regex:'" + regex + "', usesTrigger:" + usesTrigger + ", serverID: '" + serverID.serialize() + "'}")
                .as(MongoCommand.class);
        if (command == null) {
            command = new MongoCommand(regex, content, commandPermissions, serverID, usesTrigger);
        } else {
            command.setContent(content);
            command.setPermissions(EnumSetHelpers.serialize(requiredPermissions, GroupPermissions.class));
        }
        log.info("setting custom command '{}' on server {} to {} usesTrigger {}", regex, serverID.serialize(), content, usesTrigger);
        jongo.getCollection(MONGO_COMMANDS_COLLECTION).save(command);
    }

    public static void createOrModifyCurseCheckForChannel(@Nonnull String author, @Nullable String type, @Nonnull CurseGUID serverID, @Nonnull CurseGUID channelID) {
        String typesearch = "";
        if (type != null) {
            typesearch = "', type: '" + type;
        }
        MongoCurseforgeCheck check = jongo.getCollection(MONGO_CURSECHECKS_COLLECTION)
                .findOne("{author:'" + author + typesearch + "', serverID: '" + serverID.serialize() + "', channelID:'"
                        + channelID.serialize() + "'}")
                .as(MongoCurseforgeCheck.class);
        if (check == null) {
            check = new MongoCurseforgeCheck(author, type, serverID, channelID);
        } else {
            //good already
        }
        String tp2 = type;
        if (tp2 == null) {
            tp2 = "";
        }
        log.info("setting check '{}' type '{}' on server {} on channel {}", author, tp2, serverID.serialize(), channelID.serialize());
        jongo.getCollection(MONGO_CURSECHECKS_COLLECTION).save(check);
    }

    public static void removeCommandForServer(@Nonnull String regex, @Nonnull CurseGUID serverID, boolean usesTrigger) {
        log.info("removing command '{}' on server {}  usesTrigger {}", regex, serverID.serialize(), usesTrigger);
        jongo.getCollection(MONGO_COMMANDS_COLLECTION).remove("{regex:'" + regex + "', usesTrigger:" + usesTrigger + ", serverID: '" + serverID.serialize() + "'}");
    }

    public static void removeCurseforgeCheckFromServer(@Nonnull String author, @Nullable String type, @Nonnull CurseGUID serverID, @Nonnull CurseGUID channelID) {
        String tp2 = type;
        if (tp2 == null) {
            tp2 = "";
        }
        log.info("removing check '{}'  '{}' on server {}  channel {}", author, tp2, serverID.serialize(), channelID.serialize());
        String typesearch = "";
        if (type != null) {
            typesearch = "', type: '" + type;
        }
        jongo.getCollection(MONGO_CURSECHECKS_COLLECTION).remove("{author:'" + author + typesearch + "', serverID: '" + serverID.serialize() + "', channelID:'" + channelID.serialize() + "'}");
    }

    public static Optional<List<ModerationLog>> getModerationLogs(CurseGUID channelId) {
        try {
            List<ModerationLog> commandRet = new ArrayList<>();
            MongoCursor<ModerationLog> commands = jongo.getCollection(MONGO_MODERATION_LOGGING_COLLECTION).find("{channelID: '" + channelId.serialize() + "'}")
                    .as(ModerationLog.class);
            while (commands.hasNext()) {
                commandRet.add(commands.next());
            }
            commands.close();
            return Optional.of(commandRet);
        } catch (IOException | NullPointerException e) {
            log.error("error getting moderation data", e);
            return Optional.empty();
        }

    }

    public static Optional<List<ModerationLog>> getMessageLog(CurseGUID channelId, Date eventTime) {
        try {
            List<ModerationLog> commandRet = new ArrayList<>();
            MongoCursor<ModerationLog> commands = jongo.getCollection(MONGO_MODERATION_LOGGING_COLLECTION)
                    .find("{channelID: '" + channelId.serialize() + "' , 'messageTime: ' ISODate(" + eventTime.toString() + ")'}")
                    .as(ModerationLog.class);
            while (commands.hasNext()) {
                commandRet.add(commands.next());
            }
            commands.close();
            return Optional.of(commandRet);
        } catch (IOException | NullPointerException e) {
            log.error("error getting moderation data", e);
            return Optional.empty();
        }
    }

    public static Optional<List<MongoCurseforgeCheck>> getCurseChecks() {
        try {
            List<MongoCurseforgeCheck> commandRet = new ArrayList<>();
            MongoCursor<MongoCurseforgeCheck> commands = jongo.getCollection(MONGO_CURSECHECKS_COLLECTION).find()
                    .as(MongoCurseforgeCheck.class);
            while (commands.hasNext()) {
                commandRet.add(commands.next());
            }
            commands.close();
            return Optional.of(commandRet);
        } catch (IOException | NullPointerException e) {
            log.error("error getting checks", e);
            return Optional.empty();
        }
    }

    public static Optional<List<MongoCurseforgeCheck>> getCurseChecksForAuthor(@Nonnull String author) {
        try {
            List<MongoCurseforgeCheck> commandRet = new ArrayList<>();
            MongoCursor<MongoCurseforgeCheck> commands = jongo.getCollection(MONGO_COMMANDS_COLLECTION).find("{author: '" + author + "'}")
                    .as(MongoCurseforgeCheck.class);
            while (commands.hasNext()) {
                commandRet.add(commands.next());
            }
            commands.close();
            return Optional.of(commandRet);
        } catch (IOException | NullPointerException e) {
            log.error("error getting checks", e);
            return Optional.empty();
        }

    }

    @Nonnull
    //TODO should this use streams?
    public static Optional<List<MongoCommand>> getCommandsForServer(CurseGUID serverID) {
        try {
            List<MongoCommand> commandRet = new ArrayList<>();
            MongoCursor<MongoCommand> commands = jongo.getCollection(MONGO_COMMANDS_COLLECTION).find("{serverID: '" + serverID.serialize() + "'}")
                    .as(MongoCommand.class);
            while (commands.hasNext()) {
                commandRet.add(commands.next());
            }
            commands.close();
            return Optional.of(commandRet);
        } catch (IOException | NullPointerException e) {
            log.error("error getting commands for server", e);
            return Optional.empty();
        }
    }

    public static Optional<API> getAPIData(String key) {
        try {
            return Optional.of(jongo.getCollection(MONGO_API_COLLECTION).findOne("{apikey: '" + key + "'}")
                    .as(API.class));
        } catch (NullPointerException e) {
            log.error("error getting api data for key", e);
            return Optional.empty();
        }
    }

    public static void createorModifyAPIData(@Nonnull API data) {
        log.info("setting data for api key command '{}' for {} channels {}", data.getApikey(), data.getChannels().size());
        API found = jongo.getCollection(MONGO_API_COLLECTION).findOne("{apikey: '" + data.getApikey() + "'}").as(API.class);
        if (found != null) {
            found.setChannels(data.getChannels());
            jongo.getCollection(MONGO_COMMANDS_COLLECTION).save(found);
        } else {
            jongo.getCollection(MONGO_COMMANDS_COLLECTION).save(data);
        }

    }

    /**
     * this is where we would handle when DB migrations are needed past creating the main collections
     *
     * @param dbVersion version in the database
     * @param expected  version the bot is expecting
     */
    private static void migrate(VersionInfo dbVersion, VersionInfo expected) {
        //mongo supports bulk updates ... they are MUCH faster than iterating through the DB
        if (dbVersion.getVersion() == 0) {
            jongo.getCollection(MONGO_COMMANDS_COLLECTION).ensureIndex("{ serverID: 1 }");
        }
        if (dbVersion.getVersion() < 2) {
            jongo.getCollection(MONGO_CURSECHECKS_COLLECTION).ensureIndex("{author: 1}");
        }
        if (dbVersion.getVersion() < 3) {
            jongo.getCollection(MONGO_MODERATION_LOGGING_COLLECTION).ensureIndex("{serverID: 1}");
        }
        if (dbVersion.getVersion() < 4) {
            jongo.getCollection(MONGO_MODERATION_LOGGING_COLLECTION).ensureIndex("{channelID: 1}");
        }
        if (dbVersion.getVersion() < 5) {
            jongo.getCollection(MONGO_MODERATION_LOGGING_COLLECTION).ensureIndex("{channelID: 1, messageTime: -1}");
        }
        if (dbVersion.getVersion() < 6) {
            jongo.getCollection(MONGO_API_COLLECTION).ensureIndex("{apikey: 1}", "{unique: true}");
        }
        //do this last
        dbVersion.setVersion(expected.getVersion());
        jongo.getCollection(MONGO_CONFIG_COLLECTION).save(dbVersion);

    }
}
