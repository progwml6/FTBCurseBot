package com.feed_the_beast.ftbcurseappbot;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.feed_the_beast.javacurselib.CurseGUID;
import com.feed_the_beast.javacurselib.data.Apis;
import com.feed_the_beast.javacurselib.examples.app_v1.CurseApp;
import com.feed_the_beast.javacurselib.examples.app_v1.DebugResponseTask;
import com.feed_the_beast.javacurselib.examples.app_v1.DefaultResponseTask;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.service.logins.login.LoginResponse;
import com.feed_the_beast.javacurselib.service.sessions.sessions.CreateSessionResponse;
import com.feed_the_beast.javacurselib.service.sessions.sessions.DevicePlatform;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.handler.ResponseHandler;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.NotificationsServiceContractType;
import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Created by progwml6 on 5/7/16.
 */
@Slf4j
public class Main {
    public static EventBus eventBus = new EventBus();
    @Getter
    private static Optional<String> token = Optional.empty();
    @Getter
    private static Optional<ContactsResponse> contacts = Optional.empty();
    @Getter
    private static Optional<CreateSessionResponse> session = Optional.empty();
    @Getter
    public static CommentedConfigurationNode config = null;
    @Getter
    public static String botTrigger;

    public static void main (String args[]) {
        log.info("FTB CurseApp bot V 0.0.1");
        JCommander jc = null;
        try {
            jc = new JCommander(CommandArgs.getSettings(), args);
        } catch (ParameterException e) {
            log.error("Error w/ jcommander setup", e);
            System.exit(1);
        }

        if (CommandArgs.getSettings().isHelp()) {
            jc.setProgramName("FTB's CurseApp Bot");
            jc.usage();
            System.exit(0);
        }
        if (CommandArgs.getSettings().getConfigFile() == null || CommandArgs.getSettings().getConfigFile().isEmpty()) {
            log.error("need an error message");
            System.exit(1);
        }
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(new File(CommandArgs.getSettings().getConfigFile())).build(); // Create the loader
            config = loader.load(); // Load the configuration into memory
        } catch (IOException e) {
            log.error("error with config loading", e);
        }
        LoginResponse lr = CurseApp.login(config.getNode("credentials", "CurseAppLogin", "username").getString(), config.getNode("credentials", "CurseAppLogin", "password").getString());
        token = Optional.of(lr.session.token);
        if (token.isPresent()) {
            contacts = Optional.of(CurseApp.getContacts(token.get()));
            session = Optional.of(CurseApp.getSession(token.get(), CurseGUID.newRandomUUID(), DevicePlatform.UNKNOWN));
        }
        botTrigger = config.getNode("botSettings", "triggerKey").getString("!");
        log.info("bot trigger is " + botTrigger);
        // websocket testing code starts here
        WebSocket ws = null;
        try {
            ws = new WebSocket(lr, session.get(), new URI(Apis.NOTIFICATIONS));
        } catch (Exception e) {
            System.out.println("failed");
            e.printStackTrace();
            System.exit(0);
        }

        ResponseHandler responseHandler = ws.getResponseHandler();
        responseHandler.addTask(new DebugResponseTask(), NotificationsServiceContractType.CONVERSATION_MESSAGE_NOTIFICATION);
        responseHandler.addTask(new DefaultResponseTask(), NotificationsServiceContractType.CONVERSATION_READ_NOTIFICATION);
        responseHandler.addTask(new ConversationEvent(), NotificationsServiceContractType.CONVERSATION_MESSAGE_NOTIFICATION);
        responseHandler.addTask(new DebugResponseTask(), NotificationsServiceContractType.UNKNOWN);

        // to add your own handlers call ws.getResponseHandler() and configure it
        CountDownLatch latch = new CountDownLatch(1);
        ws.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }
}
