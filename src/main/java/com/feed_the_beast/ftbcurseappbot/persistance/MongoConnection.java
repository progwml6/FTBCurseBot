package com.feed_the_beast.ftbcurseappbot.persistance;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.persistance.data.VersionInfo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.jongo.Jongo;

import java.util.Arrays;

@Slf4j
public class MongoConnection {
    @Getter
    private static MongoClient client;
    @Getter
    private static MongoDatabase database;
    @Getter
    private static Jongo jongo;
    public static void start() {
        CommentedConfigurationNode config = Main.getConfig().getNode("mongo");
        if(config.getNode("enabled").getBoolean()) {
            log.info("starting up mongo client");
            String username = config.getNode("username").getString();
            String pass = config.getNode("password").getString();
            String url = config.getNode("url").getString();
            String db  = config.getNode("database").getString();

            int port = config.getNode("port").getInt(27017);
            if(username != null && !username.isEmpty() && pass != null && !pass.isEmpty()) {
                MongoCredential credential = MongoCredential.createCredential(username, db, pass.toCharArray());
                client = new MongoClient(new ServerAddress(url, port), Arrays.asList(credential));
            } else {
                client = new MongoClient(url, port);
            }
            database = client.getDatabase(db);
            jongo = new Jongo(client.getDB(db));
            log.info("started up mongo client");
            VersionInfo info  = jongo.getCollection("dbinfo").findOne("{service: 'ftbcursebot'}").as(VersionInfo.class);
            if(info == null) {
                jongo.getCollection("dbinfo").save(new VersionInfo());
                log.info("created VersionInfo for database");
            } else {
            log.info("mongo DB version: " + info.getVersion());
            }
        }
    }
}
