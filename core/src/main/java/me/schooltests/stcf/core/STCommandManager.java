package me.schooltests.stcf.core;

import me.schooltests.stcf.core.args.CommandContext;
import me.schooltests.stcf.core.command.STCommand;
import me.schooltests.stcf.core.command.SimpleSender;
import me.schooltests.stcf.core.locale.MessageLocale;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class STCommandManager {
    public void registerCommand(STCommand command) { }
    public void registerContext(CommandContext<?> context) { }

    public STCommand getRegisteredCommand(Class<? extends STCommand> clazz) { return null; }
    public CommandContext<?> getRegisteredContext(Class<?> clazz) { return null; }

    public Set<STCommand> getCommandRegistrations() { return null; }
    public Set<CommandContext<?>> getContextRegistrations() { return null; }

    public void setLocale(MessageLocale locale) { }
    public MessageLocale getLocale() { return null; }

    protected void registerDefaultContexts() {
        registerContext(new CommandContext<String>() {

            @Override
            public String transform(SimpleSender sender, Object lastValue, String value) {
                return value;
            }

            @Override
            public List<String> tabComplete(SimpleSender sender, Object lastValue, String value) {
                return new ArrayList<>();
            }

            @Override
            public Class<String> getReturnClass() {
                return String.class;
            }
        });

        registerContext(new CommandContext<Integer>() {
            @Override
            public Integer transform(SimpleSender sender, Object lastValue, String value) {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException $) {
                    return null;
                }
            }

            @Override
            public List<String> tabComplete(SimpleSender sender, Object lastValue, String value) {
                return new ArrayList<>();
            }

            @Override
            public Class<Integer> getReturnClass() {
                return Integer.class;
            }
        });

        registerContext(new CommandContext<Boolean>() {
            @Override
            public Boolean transform(SimpleSender sender, Object lastValue, String value) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("y"))
                    return true;
                else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("n"))
                    return false;
                else return null;
            }

            @Override
            public List<String> tabComplete(SimpleSender sender, Object lastValue, String value) {
                return new ArrayList<>();
            }

            @Override
            public Class<Boolean> getReturnClass() {
                return Boolean.class;
            }
        });
    }
}