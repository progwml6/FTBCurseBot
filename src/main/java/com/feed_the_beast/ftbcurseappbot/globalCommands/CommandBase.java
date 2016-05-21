package com.feed_the_beast.ftbcurseappbot.globalCommands;

import com.feed_the_beast.ftbcurseappbot.api.ICommandBase;

public abstract class CommandBase implements ICommandBase {
    protected CommandBase () {
        setup();
    }
}
