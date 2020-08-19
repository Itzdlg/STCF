package me.schooltests.stcf.core.command;

import me.schooltests.stcf.core.Pair;
import me.schooltests.stcf.core.STCommandManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"WeakerAccess"})
public abstract class STCommand {
    public final STCommandManager manager;
    protected CommandExecutor<?> executor;
    protected Map<String, CommandExecutor<?>> subExecutors;

    public final String id;
    protected List<String> aliases;
    protected String description;

    public STCommand(String id, STCommandManager manager) {
        this.manager = manager;
        this.id = id;

        this.executor = null;
        this.subExecutors = new LinkedHashMap<>();

        description = "/" + id;
        aliases = new ArrayList<>();
    }

    public final CommandExecutor<?> getExecutor() {
        return executor;
    }

    public final Map<String, CommandExecutor<?>> getSubExecutors() {
        return Collections.unmodifiableMap(subExecutors);
    }

    public final ParseResult executeCommandFromString(SimpleSender sender, String full) {
        full = full.trim();

        // s shall only contain arguments with a join of one space character
        if (subExecutors.size() == 0 && executor == null)
            return ParseResult.NO_EXECUTORS;

        Pair<String, CommandExecutor> executionPair = findExecutor(full);
        String parameters;

        if (executionPair.getKey() != null && full.length() > executionPair.getKey().trim().length())
            parameters = full.substring(executionPair.getKey().length() + 1).trim();
        else if (executionPair.getKey() == null)
            parameters = full;
        else
            parameters = "";

        CommandExecutor<?> executor = executionPair.getValue();
        if (executor == null)
            return ParseResult.NO_MATCHING_EXECUTOR;

        if (!sender.hasPermission(executor.getPermission()))
            return ParseResult.NO_PERMISSION;

        String[] arguments = parameters.split(" ");
        if (parameters.isEmpty()) arguments = new String[0];


        long numRequiredParameters = executor.getParameters().stream()
                .filter(p -> p.required)
                .count();

        if (arguments.length < numRequiredParameters)
            return ParseResult.MISSING_PARAMETER;

        if (executor.getParameters().size() <= 0) {
            executor.execute(sender, new HashMap<>());
            return ParseResult.SUCCESS;
        }

        Pair<ParseResult, Map<String, Object>> parseResult = parseTransformations(sender, executor, arguments);
        if (parseResult.getKey() == ParseResult.SUCCESS)
            executor.execute(sender, parseResult.getValue());

        return parseResult.getKey();
    }

    public final List<String> parseTabCompletion(SimpleSender sender, String[] args) {
        List<String> tabCompletions = new ArrayList<>();
        try {
            if (args == null || args.length == 0) return new ArrayList<>();

            Pair<String, CommandExecutor> executorPair = findExecutor(String.join(" ", args));
            if (executorPair.getKey() == null)
                tabCompletions.addAll(subExecutors.keySet());

            if (executorPair.getKey() != null)
                args = Arrays.copyOfRange(args, executorPair.getKey().split(" ").length, args.length);

            CommandExecutor<?> executor = executorPair.getValue();
            if (executor == null)
                return tabCompletions;

            CommandParameter parameter;
            if (executor.getParameters().size() >= args.length)
                parameter = executor.getParameters().get(args.length - 1);
            else if (executor.getParameters().size() >= 1)
                parameter = executor.getParameters().get(executor.getParameters().size() - 1);
            else
                return tabCompletions;

            if (args.length == 1) {
                tabCompletions.addAll(executor.tabComplete(sender, parameter, null, args[0]));
                return tabCompletions;
            }

            Object lastValue = null;
            for (int i = 0; i < (args.length - 1); i++) {
                CommandContext<?> context = manager.getRegisteredContext(executor.getParameters().get(i).contextClass);
                if (context != null)
                    lastValue = context.transform(sender, lastValue, args[i]);
            }

            tabCompletions.addAll(executor.tabComplete(sender, parameter, lastValue, args[args.length - 1]));
        } catch (Exception e) {
            return tabCompletions;
        }

        return tabCompletions;
    }

    private Pair<ParseResult, Map<String, Object>> parseTransformations(SimpleSender sender, CommandExecutor<?> executor, String[] arguments) {
        CommandParameter lastParameter = executor.getParameters().get(0);
        Object lastValue = null;

        Map<String, Object> transformationResults = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            String stringValue = arguments[i];
            CommandParameter parameter = lastParameter;
            if (executor.getParameters().size() > i)
                parameter = executor.getParameters().get(i);

            if (executor.getParameters().size() - 1 <= i && i != (arguments.length - 1)) {
                builder.append(stringValue).append(" ");
            } else {
                CommandContext<?> commandContext = manager.getRegisteredContext(parameter.contextClass);
                if (commandContext == null)
                    return new Pair<>(ParseResult.UNREGISTERED_CONTEXT, new HashMap<>());

                Object obj = commandContext.transform(sender, lastValue, builder.toString().isEmpty() ? stringValue : builder.toString().trim() + " " + stringValue);
                if (obj == null)
                    return new Pair<>(ParseResult.PARAMETER_TRANSFORMATION_FAILED, new HashMap<>());

                transformationResults.put(parameter.id, obj);
                lastValue = obj;
            }

            lastParameter = parameter;
        }

        return new Pair<>(ParseResult.SUCCESS, transformationResults);
    }

    private Pair<String, CommandExecutor> findExecutor(String s) {
        Optional<String> executor = subExecutors.keySet().stream()
                .sorted(Comparator.comparing(String::length))
                .filter(s::startsWith)
                .findFirst();
        return executor.map(value -> new Pair<String, CommandExecutor>(value, subExecutors.get(value))).orElseGet(() -> new Pair<>(null, this.executor));

    }

    public final List<String> getAliases() {
        return aliases;
    }

    public final String getDescription() {
        return description;
    }

    public enum ParseResult {
        SUCCESS, NO_PERMISSION, NO_MATCHING_EXECUTOR, NO_EXECUTORS, UNREGISTERED_CONTEXT, MISSING_PARAMETER, PARAMETER_TRANSFORMATION_FAILED
    }
}