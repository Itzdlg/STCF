package me.schooltests.stcf.spigot;

import me.schooltests.stcf.core.command.CommandExecutor;
import me.schooltests.stcf.core.args.CommandParameter;
import me.schooltests.stcf.core.command.ExecutionResult;
import me.schooltests.stcf.core.command.STCommand;
import me.schooltests.stcf.core.command.SimpleSender;
import me.schooltests.stcf.core.locale.MessageLocale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class SpigotCmdUtil {
    public static Command getCommandFromSTCommand(STCommand command) {
        SpigotSTCommandManager manager = (SpigotSTCommandManager) command.manager;
        MessageLocale locale = manager.getLocale();

        Command cmd = new Command(command.id, command.getDescription(), "/" + command.id, command.getAliases()) {
            @Override
            public boolean execute(CommandSender commandSender, String label, String[] args) {
                ExecutionResult result = command.executeCommandFromString(toSimpleSender(commandSender), String.join(" ", args));

                switch (result.parseResult) {
                    case MISSING_PARAMETER:
                    case PARAMETER_TRANSFORMATION_FAILED:
                    case NO_MATCHING_EXECUTOR:
                    case EXECUTION_CANCELLED:
                        commandSender.sendMessage(locale.getUsageMessage(command, result.executor, result.alias));
                        break;
                    case UNREGISTERED_CONTEXT:
                        commandSender.sendMessage(locale.getErrorMessage(command));
                        Bukkit.getLogger().severe("[STCF] " + command.id + " has a fatal error! A command parameter is using an unregistered context type!");
                        break;
                    case NO_EXECUTORS:
                        commandSender.sendMessage(locale.getErrorMessage(command));
                        Bukkit.getLogger().severe("[STCF] " + command.id + " has a fatal error! A command (" + command.id + ") has no executors!");
                        break;
                    case NO_PERMISSION:
                        commandSender.sendMessage(locale.getPermissionMessage(command, result.executor, result.executor == null ? "" : result.executor.getPermission()));
                        break;
                }

                return true;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return command.parseTabCompletion(toSimpleSender(sender), args);
            }
        };

        cmd.setUsage(locale.getUsageMessage(command, null, null));
        cmd.setAliases(command.getAliases());
        cmd.setDescription(command.getDescription());

        return cmd;
    }

    public static Player toPlayer(SimpleSender sender) {
        if (sender.getIdentifier() == null) return null;
        return Bukkit.getPlayer(UUID.fromString(sender.getIdentifier()));
    }

    public static SimpleSender toSimpleSender(CommandSender sender) {
        return new SimpleSender() {
            @Override
            public String getName() {
                return sender.getName();
            }

            @Override
            public String getIdentifier() {
                if (sender instanceof Player)
                    return ((Player) sender).getUniqueId().toString();
                else
                    return null;
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
