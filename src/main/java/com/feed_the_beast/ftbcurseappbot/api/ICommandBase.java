package com.feed_the_beast.ftbcurseappbot.api;

import com.feed_the_beast.ftbcurseappbot.CommandRegistry;
import com.feed_the_beast.ftbcurseappbot.Main;
import com.feed_the_beast.javacurselib.websocket.WebSocket;
import com.feed_the_beast.javacurselib.websocket.messages.notifications.ConversationMessageNotification;

import java.util.regex.Pattern;

/**
 * base for any command
 */
public interface ICommandBase {

    void onMessage (WebSocket webSocket, ConversationMessageNotification msg);

    default ICommandBase setup () {
        CommandRegistry.register(this);

        return this;
    }

    Pattern getTriggerRegex ();

    default Pattern getSimpleCommand (String name) {
        return Pattern.compile("(?m)^" + Main.getBotTrigger() + name + "(.*)", Pattern.CASE_INSENSITIVE);
    }

    /**
     *
     * @return The help message for this command
     */
    default String getHelp () {
        return "no help for command " + getTriggerRegex();
    }

    /**
     *
     * @return if this command can be executed in a private message or other context w/o a channel
     */
    default boolean canExecuteInPM () {
        return true;
    }

}
