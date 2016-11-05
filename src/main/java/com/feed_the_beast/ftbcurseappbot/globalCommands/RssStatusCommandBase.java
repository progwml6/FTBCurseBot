package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.utils.RssUtils;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

import javax.annotation.Nonnull;

@Slf4j
public abstract class RssStatusCommandBase extends StatusCommandBase {
    private boolean changed = false;
    private long lastUpdate = 0;

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info(getService() + "status " + msg.body.replace(Config.getBotTrigger() + getService() + "status", ""));
        webSocket.sendMessage(msg.conversationID, getServiceStatus());

    }

    @Override
    public String getHelp () {
        return "";
    }

    @Nonnull
    @Override public String getServiceStatus () {
        String ret = "";
        SyndFeed feed = RssUtils.getFeed(getFeedURL());
        if (lastUpdate == 0) {
            lastUpdate = new Date().getTime();
        }
        for (SyndEntryImpl syn : RssUtils.getFeedAfter(feed, lastUpdate)) {
            ret += feed.getTitle() + ", ";
        }
        return ret;
    }

    @Nonnull
    @Override
    public String updateServiceHealth () {
        return null;
    }

    @Override
    public boolean hasChanged () {
        return changed;
    }

    public abstract String getFeedURL ();

    private static String removeLastTwoChars (String str) {
        return str.substring(0, str.length() - 2);
    }

}
