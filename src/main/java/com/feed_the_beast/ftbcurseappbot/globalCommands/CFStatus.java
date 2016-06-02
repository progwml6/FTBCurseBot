package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.beust.jcommander.internal.Maps;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.api.statuspageio.Component;
import com.feed_the_beast.ftbcurseappbot.api.statuspageio.Status;
import com.feed_the_beast.ftbcurseappbot.api.statuspageio.StatusSummary;
import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import com.feed_the_beast.javacurselib.examples.app_v1.CurseApp;
import com.feed_the_beast.javacurselib.service.contacts.contacts.ContactsResponse;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

@Slf4j
public class CFStatus extends CommandBase {

    private static final String cfStatusAPIBase = "https://www.cloudflarestatus.com/api/";
    private static Map<String, Component> componentStatuses;
    private static Status mainStatus;

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("cfstatus " + msg.body.replace(Main.getBotTrigger() + "cfstatus", ""));
        webSocket.sendMessage(msg.conversationID, getCFStatus());
    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("cfstatus");
    }

    @Override
    public String getHelp () {
        return "gets Cloudflare status";
    }

    public static String getCFStatus () {
        updateCFHealth();
        String ret = "";
        for (Map.Entry<String, Component> me : componentStatuses.entrySet()) {
            Component c = me.getValue();
            if (!c.getStatus().equals("operational")) {
                ret += getStatusUpdate(c) + ", ";
            }
        }
        ret = "CF Status: " + mainStatus.getIndicator() + " " + mainStatus.getDescription() + ", " + ret;
        if (ret.endsWith(", ")) {
            ret = removeLastTwoChars(ret);
        }
        return ret.replace("critical", ":negative_squared_cross_mark:").replace("minor", ":construction:").replace("major", ":construction:")
                .replace("none", ":white_check_mark:").replace("major_outage", ":negative_squared_cross_mark:").replace("degraded_performance", ":construction:")
                .replace("partial_outage", ":construction:").replace("operational", ":white_check_mark:");
    }

    public static @Nonnull String updateCFHealth () {
        String ret = "";
        boolean init = false;
        try {
            StatusSummary summary = JsonFactory.GSON.fromJson(Jsoup.connect(cfStatusAPIBase + "v2/summary.json").ignoreContentType(true).get().text(), StatusSummary.class);
            if (componentStatuses == null) {
                componentStatuses = Maps.newHashMap();
                init = true;
            }
            for (Component nw : summary.components) {
                Component old = componentStatuses.get(nw.getName());
                if (old == null) {
                    ret += init ? "" : getStatusUpdate(nw) + ", ";
                    componentStatuses.put(nw.getName(), nw);
                } else {//put it in an update if its changed
                    if (old.getUpdatedAt().before(nw.getUpdatedAt())) {
                        ret += init ? "" : getStatusUpdate(nw) + ", ";
                        componentStatuses.replace(nw.getName(), nw);
                    }
                }
            }
            if (init) {
                mainStatus = summary.status;
            } else {
                if (!mainStatus.getIndicator().equals(summary.status.getIndicator()) || !mainStatus.getDescription().equals(summary.status.getDescription())) {
                    ret = "CF Status: " + summary.status.getIndicator() + " " + summary.status.getDescription() + ", " + ret;
                    mainStatus = summary.status;
                }
            }
        } catch (IOException e) {
            log.error("error getting cf status", e);
            ret = "Error getting cf status";
        }
        if (ret.endsWith(", ")) {
            ret = removeLastTwoChars(ret);
        }
        return ret.replace("major_outage", ":negative_squared_cross_mark:").replace("degraded_performance", ":construction:")
                .replace("partial_outage", ":construction:").replace("operational", ":white_check_mark:");
    }

    public static String getStatusUpdate (Component c) {
        return c.getName() + ": " + c.getStatus();
    }

    public static void sendCFStatusNotifications (@Nonnull ContactsResponse cr, @Nonnull WebSocket ws, @Nonnull String message) {
        if (message.isEmpty()) {
            log.info("no change in cf health status");
            return;
        }
        if (Main.getGHStatusChangeNotificationsEnabled().isPresent()) {
            for (String s : Main.getCFStatusChangeNotificationsEnabled().get()) {
                if (s.contains(".")) {
                    String[] g = s.split("\\.");
                    ws.sendMessage(CurseApp.getChangelIDFromChannelName(cr, g[0], g[1]), message);
                } else {
                    ws.sendMessage(CurseApp.getChangelIDFromChannelName(cr, null, s), message);
                }
            }
        }
    }

    private static String removeLastTwoChars (String str) {
        return str.substring(0, str.length() - 2);
    }

}
