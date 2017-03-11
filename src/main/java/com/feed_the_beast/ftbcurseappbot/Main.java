package com.feed_the_beast.ftbcurseappbot;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.feed_the_beast.ftbcurseappbot.persistence.CacheService;
import com.feed_the_beast.ftbcurseappbot.persistence.MongoConnection;
import com.feed_the_beast.ftbcurseappbot.runnables.BBStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.CFStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.CurseforgeChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.DynStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.GHStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.McStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.TravisStatusChecker;
import com.feed_the_beast.ftbcurseappbot.runnables.TwitchStatusChecker;
import com.feed_the_beast.ftbcurseappbot.utils.CommonMarkUtils;
import com.feed_the_beast.ftbcurseappbot.webserver.WebService;
import com.feed_the_beast.javacurselib.common.enums.DevicePlatform;
import com.feed_the_beast.javacurselib.data.Apis;
import com.feed_the_beast.javacurselib.examples.app_v1.DefaultResponseTask;
import com.feed_the_beast.javacurselib.examples.app_v1.TraceResponseTask;
import com.feed_the_beast.javacurselib.rest.RestUserEndpoints;
import com.feed_the_beast.javacurselib.service.conversations.conversations.ConversationCreateMessageRequest;
import com.feed_the_beast.javacurselib.service.logins.login.LoginRequest;
import com.feed_the_beast.javacurselib.service.logins.login.LoginResponse;
import com.feed_the_beast.javacurselib.service.sessions.sessions.CreateSessionRequest;
import com.feed_the_beast.javacurselib.service.sessions.sessions.CreateSessionResponse;
import com.feed_the_beast.javacurselib.utils.CurseGUID;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.NotificationsServiceContractType;
import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import retrofit2.adapter.java8.HttpException;

import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by progwml6 on 5/7/16.
 */
@Slf4j
public class Main {
    public static final String VERSION = "0.1.0";
    private static final int CHECKER_POLL_TIME = 120;
    public static EventBus eventBus = new EventBus();
    @Getter
    private static Optional<String> token = Optional.empty();
    @Getter
    private static Optional<CreateSessionResponse> session = Optional.empty();
    @Getter
    private static CacheService cacheService;
    @Getter
    private static RestUserEndpoints restUserEndpoints;
    @Getter
    private static CommonMarkUtils commonMarkUtils;
    @Getter
    private static WebSocket webSocket;
    @Getter
    private static ScheduledExecutorService scheduledTasks = Executors.newScheduledThreadPool(5);
    @Getter @Setter
    private static CurseGUID machineKey;//TODO this should be cached & in database
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
            log.error("need an config file");
            System.exit(1);
        }
        File configFl = new File(CommandArgs.getSettings().getConfigFile());
        if (!configFl.exists()) {
            log.error("no config file found");
            System.exit(1);
        }
        Config.load(configFl);
        restUserEndpoints = new RestUserEndpoints();
        restUserEndpoints.setupEndpoints();
        LoginResponse lr = null;

        lr = restUserEndpoints.doLogin(new LoginRequest(Config.getUsername(), Config.getPassword()));
        log.info("Synchronous login done: for user " + lr.session.username);

        CountDownLatch sessionLatch = new CountDownLatch(1);
        setMachineKey(CurseGUID.newRandomUUID());
        CompletableFuture<CreateSessionResponse> createSessionResponseCompletableFuture = restUserEndpoints.session.create(new CreateSessionRequest(getMachineKey(), DevicePlatform.UNKNOWN));

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

        token = Optional.of(lr.session.token);

        // startup persistance engine
        MongoConnection.start();
        // websocket testing code starts here
        try {
            webSocket = new WebSocket(session.get(), new URI(Apis.NOTIFICATIONS));
        } catch (Exception e) {
            log.error("websocket failed", e);
            System.exit(0);
        }
        CommandRegistry.registerBaseCommands();
        cacheService = new CacheService();
        scheduledTasks.scheduleAtFixedRate(new BBStatusChecker(webSocket), 0, CHECKER_POLL_TIME, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new CFStatusChecker(webSocket), 0, CHECKER_POLL_TIME, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new DynStatusChecker(webSocket), 0, CHECKER_POLL_TIME * 4, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new GHStatusChecker(webSocket), 0, CHECKER_POLL_TIME, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new McStatusChecker(webSocket), 0, CHECKER_POLL_TIME, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new TravisStatusChecker(webSocket), 0, CHECKER_POLL_TIME, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new TwitchStatusChecker(webSocket), 0, CHECKER_POLL_TIME * 4, TimeUnit.SECONDS);
        scheduledTasks.scheduleAtFixedRate(new CurseforgeChecker(webSocket), 0, CHECKER_POLL_TIME * 2, TimeUnit.SECONDS);

        webSocket.addTask(new ConversationEvent(), NotificationsServiceContractType.CONVERSATION_MESSAGE_NOTIFICATION);
        if (Config.isDebugEnabled()) {
            webSocket.addTask(new TraceResponseTask(), NotificationsServiceContractType.CONVERSATION_MESSAGE_NOTIFICATION);
            webSocket.addTask(new DefaultResponseTask(), NotificationsServiceContractType.CONVERSATION_READ_NOTIFICATION);
            webSocket.addTask(new TraceResponseTask(), NotificationsServiceContractType.UNKNOWN);
        }
            commonMarkUtils = new CommonMarkUtils();
        if (Config.isWebEnabled()) {
            new WebService();
        }
        // to add your own handlers call ws.getResponseHandler() and configure it
        CountDownLatch latch = new CountDownLatch(1);
        webSocket.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("latch await error", e);
        }
    }

    public static void sendMessage(CurseGUID id, String message) {
        ConversationCreateMessageRequest req = new ConversationCreateMessageRequest();
        req.body = message;
        req.machineKey=getMachineKey();
        req.clientID=CurseGUID.newRandomUUID();//TODO figure out if this should actually be random
        req.attachmentID = CurseGUID.newInstance("00000000-0000-0000-0000-000000000000");
        req.attachmentRegionID=0;
        restUserEndpoints.conversations.postMessage(id,req);
    }
}
