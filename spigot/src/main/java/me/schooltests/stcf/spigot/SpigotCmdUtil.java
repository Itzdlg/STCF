package me.schooltests.stcf.spigot;

import me.schooltests.stcf.core.command.CommandExecutor;
import me.schooltests.stcf.core.command.CommandParameter;
import me.schooltests.stcf.core.command.STCommand;
import me.schooltests.stcf.core.command.SimpleSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

public class SpigotCmdUtil {
    public static Command getCommandFromSTCommand(STCommand command) {
        Command cmd = new Command(command.id, command.getDescription(), "/" + command.id, command.getAliases()) {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                STCommand.ParseResult result = command.executeCommandFromString(toSimpleSender(commandSender), String.join(" ", args));

                switch (result) {
                    case MISSING_PARAMETER:
                    case PARAMETER_TRANSFORMATION_FAILED:
                    case NO_MATCHING_EXECUTOR:
                        commandSender.sendMessage(getUsageMessage(command));
                        break;
                    case UNREGISTERED_CONTEXT:
                        commandSender.sendMessage(ChatColor.RED + "An error occurred with the command!");
                        Bukkit.getLogger().severe("[STCF] " + command.id + " has a fatal error! A command parameter is using an unregistered context type!");
                        break;
                    case NO_EXECUTORS:
                        commandSender.sendMessage(ChatColor.RED + "An error occurred with the command!");
                        Bukkit.getLogger().severe("[STCF] " + command.id + " has a fatal error! A command (" + command.id + ") has no executors!");
                        break;
                    case NO_PERMISSION:
                        commandSender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ((SpigotSTCommandManager) command.manager).plugin + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "You are missing the required permissions for this command!");
                        break;
                }

                return true;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return command.parseTabCompletion(toSimpleSender(sender), args);
            }
        };

        cmd.setUsage(getUsageMessage(command));
        cmd.setAliases(command.getAliases());
        cmd.setDescription(command.getDescription());

        return cmd;
    }

    public static String getUsageMessage(STCommand command) {
        BiConsumer<StringBuilder, CommandParameter> appendRule = (message, parameter) ->
                message.append(" ").append(parameter.required ? "<" : "[").append(parameter.id).append(parameter.required ? ">" : "]");

        StringBuilder message = new StringBuilder();
        message.append("\n").append(ChatColor.GRAY).append("Usage for ").append(ChatColor.GOLD).append("/").append(command.id).append("\n");
        message.append(ChatColor.GRAY).append("-----------------------------------").append("\n");
        message.append(ChatColor.GRAY).append("/").append(command.id).append(ChatColor.GOLD);
        for (CommandParameter parameter : command.getExecutor().getParameters())
            appendRule.accept(message, parameter);

        for (String key : command.getSubExecutors().keySet()) {
            CommandExecutor<?> executor = command.getSubExecutors().get(key);
            message.append("\n").append(ChatColor.GRAY).append("/").append(command.id).append(" ").append(key).append(ChatColor.GOLD);
            for (CommandParameter parameter : executor.getParameters())
                appendRule.accept(message, parameter);
        }

        message.append("\n").append(ChatColor.GRAY).append("-----------------------------------").append("\n");

        return message.toString();
    }

    public static Player toPlayer(SimpleSender sender) {
        if (sender.getName().equalsIgnoreCase("CONSOLE")) return null;
        return Bukkit.getPlayerExact(sender.getName());
    }

    public static SimpleSender toSimpleSender(CommandSender sender) {
        return new SimpleSender() {
            @Override
            public String getName() {
                return sender.getName();
            }

            @Override
            public void sendMessage(String s) {
                sender.sendMessage(s);
            }

            @Override
            public boolean hasPermission(String s) {
                return sender instanceof ConsoleCommandSender || sender.isOp() || sender.hasPermission(s) || s.isEmpty();
            }
        };
    }
}
