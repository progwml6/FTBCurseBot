package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Config;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.ftbcurseappbot.api.ftbsupportfaq.FaqData;
import com.feed_the_beast.ftbcurseappbot.api.ftbsupportfaq.Sitejson;
import com.feed_the_beast.ftbcurseappbot.utils.JsonFactory;
import com.feed_the_beast.ftbcurseappbot.utils.NetworkingUtils;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

@Slf4j
public class FTBFaq extends CommandBase {
    private static final String SUPPORT_FAQ_JSON = "http://support.feed-the-beast.com/faq/sitejson.json";

    private static String removeLastTwoChars (String str) {
        return str.substring(0, str.length() - 2);
    }

    @Nullable
    private static Sitejson getFaqData () {
        String faq = null;
        try {
            faq = NetworkingUtils.getSynchronous(SUPPORT_FAQ_JSON);
            return JsonFactory.GSON.fromJson(faq, Sitejson.class);
        } catch (Exception e) {
            log.error("error getting support faq ", e);
        }
        return null;
    }

    @Override public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("ftbfaq " + msg.body.replace(Config.getBotTrigger() + "ftbfaq", ""));
        String[] params = StringUtils.split(msg.body, " ");
        if (params.length > 1) {
            boolean list = false;
            String param = params[1];
            if (param.equalsIgnoreCase("list")) {
                list = true;
            }
            Sitejson sitejson = getFaqData();
            if (sitejson != null) {
                List<FaqData> data = sitejson.faqtopics.data;
                if (list) {
                    StringBuilder bdr = new StringBuilder().append("FAQ Topics: ");
                    for (FaqData entry : data) {
                        bdr.append(entry.name).append(", ");
                    }
                    Main.sendMessage(msg.conversationID, removeLastTwoChars(bdr.toString()));

                } else {
                    Optional<FaqData> res = data.stream().filter(entry -> entry.name.equalsIgnoreCase(param)).findFirst();
                    if (res.isPresent()) {
                        Main.sendMessage(msg.conversationID, res.get().title + ": " + res.get().url);
                    } else {
                        Main.sendMessage(msg.conversationID, "topic " + param + " doesn't exist");
                    }
                }
            } else {
                log.error("faqdata was null");
            }
        } else {
            Main.sendMessage(msg.conversationID, getHelp());
        }

    }

    @Override
    public Pattern getTriggerRegex () {
        return getSimpleCommand("ftbfaq");
    }

    @Override
    public String getHelp () {
        return "ftbfaq <command> to link to a FAQ topic or ftbfaq list to list the FAQ topics";
    }

}
