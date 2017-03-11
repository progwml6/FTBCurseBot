package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.api.statuspageio.Component;
import com.feed_the_beast.ftbcurseappbot.api.statuspageio.Status;
import com.feed_the_beast.ftbcurseappbot.api.statuspageio.StatusSummary;
import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import com.feed_the_beast.ftbcurseappbot.utils.NetworkingUtils;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

@Slf4j
public abstract class StatusPageIOBase extends StatusCommandBase {

    private Map<String, Component> componentStatuses;
    private Status mainStatus;
    private boolean changed = false;
    private String serviceName;

    private static String getStatusUpdate (Component c) {
        return c.getName() + ": " + c.getStatus();
    }

    private static String removeLastTwoChars (String str) {
        return str.substring(0, str.length() - 2);
    }

    public abstract String getBaseURL ();

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info(getService() + "status " + msg.body.replace(Config.getBotTrigger() + getService() + "status", ""));
        Main.sendMessage(msg.conversationID, getServiceStatus());
    }

    @Nonnull
    public String getServiceStatus () {
        updateServiceHealth();
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, Component> me : componentStatuses.entrySet()) {
            Component c = me.getValue();
            if (!c.getStatus().equals("operational")) {
                buf.append(getStatusUpdate(c)).append(", ");
            }
        }
        String ret = serviceName + " Status: " + mainStatus.getIndicator() + " " + mainStatus.getDescription() + ", " + buf.toString();
        if (ret.endsWith(", ")) {
            ret = removeLastTwoChars(ret);
        }
        return ret.replace("critical", ":negative_squared_cross_mark:").replace("minor", ":construction:").replace("none", ":white_check_mark:")
                .replace("major_outage", ":negative_squared_cross_mark:").replace("degraded_performance", ":construction:").replace("partial_outage", ":construction:")
                .replace("operational", ":white_check_mark:").replace("major", ":construction:").replace("under_maintenance", ":construction:");
    }

    public @Nonnull String updateServiceHealth () {
        String ret = "";
        boolean init = false;
        changed = false;
        try {
            StatusSummary summary = JsonFactory.GSON.fromJson(NetworkingUtils.getSynchronous(getBaseURL() + "v2/summary.json"), StatusSummary.class);
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
                        changed = true;
                        componentStatuses.replace(nw.getName(), nw);
                    }
                }
            }
            if (init) {
                mainStatus = summary.status;
                serviceName = summary.page.getName();
            } else {
                if (changed || !mainStatus.getIndicator().equals(summary.status.getIndicator()) || !mainStatus.getDescription().equals(summary.status.getDescription())) {
                    ret = getService() + " Status: " + summary.status.getDescription() + ", " + ret;
                    changed = true;
                    mainStatus = summary.status;
                }
            }
        } catch (IOException e) {
            log.error("error getting " + getService() + " status", e);
            ret = "Error getting " + getService() + " status";
        }
        if (ret.endsWith(", ")) {
            ret = removeLastTwoChars(ret);
        }
        return ret.replace("critical", ":negative_squared_cross_mark:").replace("minor", ":construction:").replace("none", ":white_check_mark:")
                .replace("major_outage", ":negative_squared_cross_mark:").replace("degraded_performance", ":construction:").replace("partial_outage", ":construction:")
                .replace("operational", ":white_check_mark:").replace("major", ":construction:").replace("under_maintenance", ":construction:");
    }

    @Override
    public boolean hasChanged () {
        return changed;
    }

}
