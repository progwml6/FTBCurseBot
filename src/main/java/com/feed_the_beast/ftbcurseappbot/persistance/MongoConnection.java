package com.feed_the_beast.ftbcurseappbot.persistance;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistance.data.VersionInfo;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.jongo.Jongo;

import java.util.Collections;

import javax.annotation.Nullable;

@Slf4j
public class MongoConnection {
    @Getter
    private static MongoClient client;
    @Getter
    private static MongoDatabase database;
    @Getter
    private static Jongo jongo;

    public static void start () {
        CommentedConfigurationNode config = Main.getConfig().getNode("mongo");
        if (config.getNode("enabled").getBoolean()) {
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
            VersionInfo info = jongo.getCollection("dbinfo").findOne("{service: 'ftbcursebot'}").as(VersionInfo.class);
            if (info == null) {
                info = new VersionInfo();
                jongo.getCollection("dbinfo").save(info);
                log.info("created VersionInfo for database");
            } else {
                log.info("mongo DB version: " + info.getVersion());
            }
            VersionInfo current = new VersionInfo();
            if (info.getVersion() < current.getVersion()) {
                log.info("database needs to be migrated from version " + info.getVersion() + " to " + current.getVersion());
                migrate(info, current);
            }
        }
    }

    public static void logEvent (PersistanceEventType event, CurseGUID serverID, @Nullable CurseGUID channel, long performer, long affects, String info) {

    }

    /**
     * this is where we would handle when DB migrations are needed past creating the main collections
     * @param dbVersion version in the database
     * @param expected version the bot is expecting
     */
    private static void migrate (VersionInfo dbVersion, VersionInfo expected) {
        jongo.getCollection("dbinfo").save(expected.getVersion());
    }
}
