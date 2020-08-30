package me.schooltests.stcf.core.command;

import me.schooltests.stcf.core.STCommandManager;

import java.util.Collections;
import java.util.List;

public abstract class CommandExecutor<T extends STCommand> {
    public final T command;
    public final STCommandManager manager;

    public CommandExecutor(T command) {
        this.command = command;
        this.manager = command.manager;
    }

    public List<CommandParameter> getParameters() {
        return Collections.emptyList();
    }

    public String getPermission() {
        return "";
    }

    public abstract void execute(SimpleSender sender, CommandArguments args);

    public List<String> tabComplete(SimpleSender sender, CommandParameter parameter, Object lastValue, String value) {
        return manager.getRegisteredContext(parameter.contextClass).tabComplete(sender, lastValue, value);
    }
}