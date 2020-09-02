package me.schooltests.stcf.spigot;

import me.schooltests.stcf.core.args.CommandParameter;
import me.schooltests.stcf.core.command.CommandExecutor;
import me.schooltests.stcf.core.command.STCommand;
import me.schooltests.stcf.core.locale.MessageLocale;
import org.bukkit.ChatColor;

import java.util.function.BiConsumer;

public class SpigotLocale implements MessageLocale {
    private final BiConsumer<StringBuilder, CommandParameter> appendRule = (message, parameter) ->
            message.append(" ").append(parameter.required ? "<" : "[").append(parameter.id).append(parameter.required ? ">" : "]");

    @Override
    public String getUsageMessage(STCommand command, CommandExecutor<?> executor, String alias) {
        if (executor == null) {
            StringBuilder message = new StringBuilder();
            message.append("\n").append(ChatColor.GRAY).append("Usage for ").append(ChatColor.GOLD).append("/").append(command.id).append("\n");
            message.append(ChatColor.GRAY).append("-----------------------------------").append("\n");
            message.append(ChatColor.GRAY).append("/").append(command.id).append(ChatColor.GOLD);
            for (CommandParameter parameter : command.getExecutor().getParameters())
                appendRule.accept(message, parameter);

            for (String key : command.getSubExecutors().keySet()) {
                CommandExecutor<?> iExecutor = command.getSubExecutors().get(key);
                message.append("\n").append(ChatColor.GRAY).append("/").append(command.id).append(" ").append(key).append(ChatColor.GOLD);
                for (CommandParameter parameter : iExecutor.getParameters())
                    appendRule.accept(message, parameter);
            }

            message.append("\n").append(ChatColor.GRAY).append("-----------------------------------").append("\n");

            return message.toString();
        }

        StringBuilder cmd = new StringBuilder("/" + command.id + ((alias != null) ? alias : ""));
        for (CommandParameter p : executor.getParameters())
            appendRule.accept(cmd, p);

        return ChatColor.GRAY + "Usage: " + ChatColor.GOLD + cmd;
    }

    @Override
    public String getPermissionMessage(STCommand command, CommandExecutor<?> executor, String permission) {
        return ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ((SpigotSTCommandManager) command.manager).plugin.getName() + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "You are missing the required permissions for this command!";
    }

    @Override
    public String getErrorMessage(STCommand command) {
        return ChatColor.RED + "An error has occurred, tell an administrator to check their server console";
    }
}
