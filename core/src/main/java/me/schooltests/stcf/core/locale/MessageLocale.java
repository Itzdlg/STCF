package me.schooltests.stcf.core.locale;

import me.schooltests.stcf.core.command.CommandExecutor;
import me.schooltests.stcf.core.command.STCommand;

public interface MessageLocale {
    String getUsageMessage(STCommand command, CommandExecutor<?> executor, String alias);
    String getPermissionMessage(STCommand command, CommandExecutor<?> executor, String permission);
    String getErrorMessage(STCommand command);
}
