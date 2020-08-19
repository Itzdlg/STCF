package me.schooltests.stcf.spigot;

import me.schooltests.stcf.core.STCommandManager;
import me.schooltests.stcf.core.command.CommandContext;
import me.schooltests.stcf.core.command.STCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SpigotSTCommandManager extends STCommandManager {
    private static CommandMap commandMap = null;

    public final JavaPlugin plugin;
    private final Set<STCommand> commands;
    private final Set<CommandContext<?>> contexts;

    static {
        try {
            final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);

            commandMap = (CommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public SpigotSTCommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commands = new HashSet<>();
        this.contexts = new HashSet<>();

        registerDefaultContexts();
    }

    @Override
    public void registerCommand(STCommand command) {
        commandMap.register(plugin.getName(), SpigotCmdUtil.getCommandFromSTCommand(command));
        commands.add(command);
    }

    @Override
    public void registerContext(CommandContext context) {
        contexts.add(context);
    }

    @Override
    public STCommand getRegisteredCommand(Class<? extends STCommand> clazz) {
        return commands.stream().filter(c -> c.getClass().equals(clazz)).findFirst().orElse(null);
    }

    @Override
    public CommandContext<?> getRegisteredContext(Class clazz) {
        return contexts.stream().filter(c -> c.getReturnClass().equals(clazz)).findFirst().orElse(null);
    }

    @Override
    public Set<STCommand> getCommandRegistrations() {
        return Collections.unmodifiableSet(commands);
    }

    @Override
    public Set<CommandContext<?>> getContextRegistrations() {
        return Collections.unmodifiableSet(contexts);
    }
}