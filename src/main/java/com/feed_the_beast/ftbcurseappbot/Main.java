package com.feed_the_beast.ftbcurseappbot;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.feed_the_beast.ftbcurseappbot.persistance.CacheService;
import com.feed_the_beast.ftbcurseappbot.persistance.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.runnables.BBStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.CFStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.GHStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.McStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.TravisStatusChecker;
import com.feed_the_beast.ftbcurseappbot.webserver.WebService;
import com.feed_the_beast.javacurselib.common.enums.DevicePlatform;
import com.feed_the_beast.javacurselib.data.Apis;
import com.feed_the_beast.javacurselib.examples.app_v1.DefaultResponseTask;
import com.feed_the_beast.javacurselib.examples.app_v1.TraceResponseTask;
import com.feed_the_beast.javacurselib.rest.REST;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.service.logins.login.LoginRequest;
import com.feed_the_beast.javacurselib.service.logins.login.LoginResponse;
import com.feed_the_beast.javacurselib.service.sessions.sessions.CreateSessionRequest;
import com.feed_the_beast.javacurselib.service.sessions.sessions.CreateSessionResponse;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.handler.ResponseHandler;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.NotificationsServiceContractType;
import com.google.common.eventbus.EventBus;
import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import retrofit2.adapter.java8.HttpException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by progwml6 on 5/7/16.
 */
@Slf4j
public class Main {
    public static final String VERSION = "0.0.1";
    public static EventBus eventBus = new EventBus();
    @Getter
    private static boolean debug;
    @Getter
    private static String username = null;
    @Getter
    private static Optional<String> token = Optional.empty();
    @Getter
    private static Optional<ContactsResponse> contacts = Optional.empty();
    @Getter
    private static Optional<CreateSessionResponse> session = Optional.empty();
    @Getter
    private static CacheService cacheService;
    @Getter
    private static CommentedConfigurationNode config = null;
    @Getter
    private static String botTrigger;
    @Getter
    private static Optional<List<String>> bBStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> cFStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> mcStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> gHStatusChangeNotificationsEnabled = Optional.empty();
    @Getter
    private static Optional<List<String>> travisStatusChangeNotificationsEnabled = Optional.empty();

    @Getter
    private static WebSocket webSocket;
    @Getter
    private static ScheduledExecutorService scheduledTasks = Executors.newScheduledThreadPool(5);

    public static void main (String args[]) {
        log.info("FTB CurseApp bot V " + VERSION);
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
        LoginResponse lr = null;

        try {
            username = config.getNode("credentials", "CurseAppLogin", "username").getString();
            lr = REST.login.login(new LoginRequest(username, config.getNode("credentials", "CurseAppLogin", "password").getString()))
                    .get();
        } catch (InterruptedException e) {
            // should not happen, just ignore
        } catch (ExecutionException e) {
            if (e.getCause() instanceof HttpException) {
                log.error("Request failed: HTTP code: " + ((HttpException) e.getCause()).code());
                // TODO: add helper function(s) to verbosely debug fail reason(s) and/or check if retrofit/okhttp logging
            } else {
                // network or  parser error, just print exception with causes
                log.error("error logging in", e);
            }
            System.exit(1);
        }
        log.info("Synchronous login done: for user " + lr.session.username);

        // TODO: fix this by making REST fully non-static class and/or using other proper design patterns
        REST.setAuthToken(lr.session.token);

        CountDownLatch sessionLatch = new CountDownLatch(1);

        CompletableFuture<CreateSessionResponse> createSessionResponseCompletableFuture = REST.session.create(new CreateSessionRequest(CurseGUID.newRandomUUID(), DevicePlatform.UNKNOWN));

        createSessionResponseCompletableFuture.whenComplete((r, e) -> {
            if (e != null) {
                if (e.getCause() instanceof HttpException) {
                    log.error("Request failed: HTTP code: " + ((HttpException) e.getCause()).code());
                    // TODO: see comment in login response
                } else {
                    // network or  parser error, just print exception with causes
                    log.error("Request failed", e);
                }
                System.exit(1);
            }

            // all is ok. Set value
            session = Optional.of(r);
            // and make man thread to continue again
            sessionLatch.countDown();
        });

        // ugly code/synchronization just to implement example
        try {
            sessionLatch.await();
            // as soon as lock opened we know that sessionResponse is usable and it is safe to start websocket
        } catch (InterruptedException e) {
            System.exit(1);
            // should not happen, just ignore
        }
        log.info("Async session done: " + session.get());

        /***************************
         *  experiment with data.
         ***************************/

        contacts = Optional.of(REST.contacts.get().join()); // wil throw RuntimeException if fails
        token = Optional.of(lr.session.token);

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
        log.info("bot trigger is " + botTrigger);
        // startup persistance engine
        MongoConnection.start();
        // websocket testing code starts here
        try {
            webSocket = new WebSocket(lr, session.get(), new URI(Apis.NOTIFICATIONS));
        } catch (Exception e) {
            log.error("websocket failed", e);
            System.exit(0);
        }
        CommandRegistry.registerBaseCommands();
        scheduledTasks.scheduleAtFixedRate(new BBStatusChecker(webSocket, contacts.get()), 0, 60, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new CFStatusChecker(webSocket, contacts.get()), 0, 60, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new GHStatusChecker(webSocket, contacts.get()), 0, 60, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new McStatusChecker(webSocket, contacts.get()), 0, 60, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new TravisStatusChecker(webSocket, contacts.get()), 0, 60, TimeUnit.SECONDS);
        cacheService = new CacheService();

        ResponseHandler responseHandler = webSocket.getResponseHandler();
        responseHandler.addTask(new ConversationEvent(), NotificationsServiceContractType.CONVERSATION_MESSAGE_NOTIFICATION);
        debug = config.getNode("botSettings", "debug").getBoolean(false);
        if (debug) {
            responseHandler.addTask(new TraceResponseTask(), NotificationsServiceContractType.CONVERSATION_MESSAGE_NOTIFICATION);
            responseHandler.addTask(new DefaultResponseTask(), NotificationsServiceContractType.CONVERSATION_READ_NOTIFICATION);
            responseHandler.addTask(new TraceResponseTask(), NotificationsServiceContractType.UNKNOWN);
        }
        if (config.getNode("botSettings", "webEnabled").getBoolean(true)) {
            WebService service = new WebService();
        }
        // to add your own handlers call ws.getResponseHandler() and configure it
        CountDownLatch latch = new CountDownLatch(1);
        webSocket.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }
}
