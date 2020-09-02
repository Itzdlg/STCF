package me.schooltests.stcf.spigot;

import me.schooltests.stcf.core.STCommandManager;
import me.schooltests.stcf.core.args.CommandContext;
import me.schooltests.stcf.core.command.STCommand;
import me.schooltests.stcf.core.command.SimpleSender;
import me.schooltests.stcf.core.locale.MessageLocale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpigotSTCommandManager extends STCommandManager {
    private static CommandMap commandMap = null;

    public final JavaPlugin plugin;
    private final Set<STCommand> commands;
    private final Set<CommandContext<?>> contexts;

    private MessageLocale locale;

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
        this.locale = new SpigotLocale();

        registerDefaultContexts();
        registerSpigotContexts();
    }

    @Override
    public void registerCommand(STCommand command) {
        commandMap.register(plugin.getName(), SpigotCmdUtil.getCommandFromSTCommand(command));
        commands.add(command);
    }

    @Override
    public void registerContext(CommandContext<?> context) {
        contexts.add(context);
    }

    @Override
    public STCommand getRegisteredCommand(Class<? extends STCommand> clazz) {
        return commands.stream().filter(c -> c.getClass().equals(clazz)).findFirst().orElse(null);
    }

    @Override
    public CommandContext<?> getRegisteredContext(Class<?> clazz) {
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

    @Override
    public MessageLocale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(MessageLocale locale) {
        this.locale = locale;
    }

    private void registerSpigotContexts() {
        registerContext(new CommandContext<Player>() {
            @Override
            public Player transform(SimpleSender sender, Object lastValue, String value) {
                List<Player> matches = Bukkit.matchPlayer(value);
                if (matches.size() == 0) return null;

                return Bukkit.matchPlayer(value).get(0);
            }

            @Override
            public List<String> tabComplete(SimpleSender sender, Object lastValue, String value) {
                List<String> players = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));

                return players;
            }

            @Override
            public Class<Player> getReturnClass() {
                return Player.class;
            }
        });

        registerContext(new CommandContext<OfflinePlayer>() {
            @Override
            public OfflinePlayer transform(SimpleSender sender, Object lastValue, String value) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(value);
                if (p.hasPlayedBefore()) return null;
                return p;
            }

            @Override
            public List<String> tabComplete(SimpleSender sender, Object lastValue, String value) {
                List<String> players = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));

                return players;
            }

            @Override
            public Class<OfflinePlayer> getReturnClass() {
                return OfflinePlayer.class;
            }
        });

        registerContext(new CommandContext<World>() {
            @Override
            public World transform(SimpleSender sender, Object lastValue, String value) {
                return Bukkit.getWorlds().stream()
                        .sorted(Comparator.comparing(w -> w.getName().length()))
                        .filter(w -> w.getName().startsWith(value))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public List<String> tabComplete(SimpleSender sender, Object lastValue, String value) {
                return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }

            @Override
            public Class<World> getReturnClass() {
                return World.class;
            }
        });

        registerContext(new CommandContext<Material>() {
            @Override
            public Material transform(SimpleSender sender, Object lastValue, String value) {
                return Arrays.stream(Material.values())
                        .sorted(Comparator.comparing(m -> m.name().length()))
                        .filter(m -> m.name().startsWith(value))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public List<String> tabComplete(SimpleSender sender, Object lastValue, String value) {
                return Arrays.stream(Material.values()).map(Material::name).filter(n -> !n.startsWith("LEGACY_")).collect(Collectors.toList());
            }

            @Override
            public Class<Material> getReturnClass() {
                return Material.class;
            }
        });
    }
}