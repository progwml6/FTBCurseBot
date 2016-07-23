package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

@Slf4j
public class MCStatus extends StatusCommandBase {
    public static StatusCommandBase instance;

    public MCStatus () {
        instance = this;
    }

    public static StatusCommandBase getInstance () {
        return instance;
    }

    @Override
    public void onMessage (WebSocket webSocket, ConversationMessageNotification msg) {
        log.info("mcstatus " + msg.body.replace(Main.getBotTrigger() + "mcstatus", ""));
        webSocket.sendMessage(msg.conversationID, getServiceStatus());
    }

    @Override public String getService () {
        return "mc";
    }

    @Override
    public String getHelp () {
        return "gets the health status of MC services from mojang";
    }

    @Nonnull
    @Override
    public String getServiceStatus () {
        updateServiceHealth();
        if (mcstatushealth == null || mcstatushealth.isEmpty()) {
            return "ERROR getting mc status";
        }
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> me : mcstatushealth.entrySet()) {
            buf.append(getPrettyStatus(me.getKey(), true));
        }
        String ret = buf.toString();
        if (ret.endsWith("|")) {
            ret = removeLastChar(ret);
        }
        return ret;
    }

    private static Map<String, String> mcstatusmappings = null;
    private static Map<String, String> mcstatushealth = null;

    @Override
    public @Nonnull String updateServiceHealth () {
        try {
            String json = Jsoup.connect("http://status.mojang.com/check").ignoreContentType(true).get().text();
            JsonParser p = new JsonParser();
            JsonArray report = p.parse(json).getAsJsonArray();
            if (mcstatusmappings == null) {
                initMappings();
            }
            if (mcstatushealth == null) {
                mcstatushealth = Maps.newHashMap();
            }
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < report.size(); i++) {
                for (Map.Entry<String, JsonElement> a : report.get(i).getAsJsonObject().entrySet()) {
                    //log.info(a.getKey() + " " + mcstatusmappings.get(a.getKey()) + " " + a.getValue().getAsString());
                    String cstatus = mcstatushealth.get(mcstatusmappings.get(a.getKey()));
                    mcstatushealth.put(mcstatusmappings.get(a.getKey()), a.getValue().getAsString());
                    if (cstatus != null && !cstatus.isEmpty() && !cstatus.equals(a.getValue().getAsString())) {
                        buf.append(getPrettyStatus(a.getKey(), true));
                    }
                }
            }
            String changelist = buf.toString();
            if (!changelist.isEmpty()) {
                if (changelist.endsWith("|")) {
                    changelist = removeLastChar(changelist);
                }
                return "MC Status Has Changed to: " + changelist;
            }
        } catch (IOException e) {
            log.error("error getting mc status", e);
        }
        return "";
    }

    public static String getPrettyStatus (String item, boolean needPipe) {
        String ret = getMCStatusFor(item);
        if (!ret.isEmpty()) {
            String pretty = mcstatusmappings.containsKey(item) ? mcstatusmappings.get(item) : item;
            return pretty + ": " + ret + (needPipe ? "|" : "");
        }
        return "";
    }

    @Nonnull
    public static String getMCStatusFor (String s) {
        if (mcstatusmappings == null) {
            initMappings();
        }

        String ret = (mcstatushealth.containsKey(s) ? mcstatushealth.get(s) : mcstatushealth.get(mcstatusmappings.get(s)));
        if (ret == null) {
            ret = "";
        }
        return ret.replace("up", "green").replace("down", "red").replace("problem", "yellow").replace("red", ":negative_squared_cross_mark:").replace("yellow", ":construction:")
                .replace("green", ":white_check_mark:");

    }

    public static void initMappings () {
        mcstatusmappings = Maps.newHashMap();
        mcstatusmappings.put("mojang.com", "Mojang.com");
        mcstatusmappings.put("textures.minecraft.net", "Textures");
        mcstatusmappings.put("api.mojang.com", "Public API");
        mcstatusmappings.put("authserver.mojang.com", "Auth Server");
        mcstatusmappings.put("skins.minecraft.net", "Skins");
        mcstatusmappings.put("auth.mojang.com", "Mojang Auth");
        mcstatusmappings.put("account.mojang.com", "Accounts website");
        mcstatusmappings.put("minecraft.net", "Minecraft.net");
        mcstatusmappings.put("session.minecraft.net", "Legacy Session");
        mcstatusmappings.put("sessionserver.mojang.com", "Session");
        mcstatusmappings.put("login", "Auth Server");//xpaw
    }

    private static String removeLastChar (String str) {
        return str.substring(0, str.length() - 1);
    }

}
