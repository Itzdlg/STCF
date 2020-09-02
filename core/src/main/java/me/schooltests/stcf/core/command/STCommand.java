package me.schooltests.stcf.core.command;

import me.schooltests.stcf.core.Pair;
import me.schooltests.stcf.core.STCommandManager;
import me.schooltests.stcf.core.args.CommandArguments;
import me.schooltests.stcf.core.args.CommandContext;
import me.schooltests.stcf.core.args.CommandParameter;

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

    private Map<String, CommandExecutor<?>> subExecutors;
    private Map<String, List<String>> subExecutorsAliases;

    public final String id;
    protected List<String> aliases;
    protected String description;

    public STCommand(String id, STCommandManager manager) {
        this.manager = manager;
        this.id = id;

        this.executor = null;
        this.subExecutors = new LinkedHashMap<>();
        this.subExecutorsAliases = new HashMap<>();

        description = "/" + id;
        aliases = new ArrayList<>();
    }

    public final CommandExecutor<?> getExecutor() {
        return executor;
    }

    public final Map<String, CommandExecutor<?>> getSubExecutors() {
        return Collections.unmodifiableMap(subExecutors);
    }

    public final void addSubCommand(String id, CommandExecutor<?> executor, String... aliases) {
        subExecutors.put(id, executor);
        if (aliases.length > 0)
            subExecutorsAliases.put(id, Arrays.asList(aliases));
    }

    public final ExecutionResult executeCommandFromString(SimpleSender sender, String full) {
        full = full.trim();

        // s shall only contain arguments with a join of one space character
        if (subExecutors.size() == 0 && executor == null)
            return new ExecutionResult(this, ParseResult.NO_EXECUTORS, null, null);

        Pair<String, CommandExecutor<?>> executionPair = findExecutor(full);
        String parameters;

        if (executionPair.getKey() != null && full.length() > executionPair.getKey().trim().length())
            parameters = full.substring(executionPair.getKey().length() + 1).trim();
        else if (executionPair.getKey() == null)
            parameters = full;
        else
            parameters = "";

        CommandExecutor<?> executor = executionPair.getValue();
        if (executor == null)
            return new ExecutionResult(this, ParseResult.NO_MATCHING_EXECUTOR, null, null);

        if (!sender.hasPermission(executor.getPermission()))
            return new ExecutionResult(this, ParseResult.NO_PERMISSION, executor, executionPair.getKey());

        String[] arguments = parameters.split(" ");
        if (parameters.isEmpty()) arguments = new String[0];


        long numRequiredParameters = executor.getParameters().stream()
                .filter(p -> p.required)
                .count();

        if (arguments.length < numRequiredParameters)
            return new ExecutionResult(this, ParseResult.MISSING_PARAMETER, executor, executionPair.getKey());

        if (executor.getParameters().size() <= 0) {
            boolean success = executor.execute(sender, new CommandArguments(new LinkedHashMap<>()));
            if (!success) return new ExecutionResult(this, ParseResult.EXECUTION_CANCELLED, executor, executionPair.getKey());
            return new ExecutionResult(this, ParseResult.SUCCESS, executor, executionPair.getKey());
        }

        Pair<ParseResult, LinkedHashMap<String, Object>> parseResult = parseTransformations(sender, executor, arguments);
        if (parseResult.getKey() == ParseResult.SUCCESS) {
            boolean success = executor.execute(sender, new CommandArguments(parseResult.getValue()));
            if (!success) return new ExecutionResult(this, ParseResult.EXECUTION_CANCELLED, executor, executionPair.getKey());
            return new ExecutionResult(this, ParseResult.SUCCESS, executor, executionPair.getKey());
        }

        return new ExecutionResult(this, parseResult.getKey(), executor, executionPair.getKey());
    }

    public final List<String> parseTabCompletion(SimpleSender sender, String[] args) {
        List<String> tabCompletions = new ArrayList<>();
        try {
            if (args == null || args.length == 0) return new ArrayList<>();

            Pair<String, CommandExecutor<?>> executorPair = findExecutor(String.join(" ", args));
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

    private Pair<ParseResult, LinkedHashMap<String, Object>> parseTransformations(SimpleSender sender, CommandExecutor<?> executor, String[] arguments) {
        CommandParameter lastParameter = executor.getParameters().get(0);
        Object lastValue = null;

        LinkedHashMap<String, Object> transformationResults = new LinkedHashMap<>();
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
                    return new Pair<>(ParseResult.UNREGISTERED_CONTEXT, new LinkedHashMap<>());

                Object obj = commandContext.transform(sender, lastValue, builder.toString().isEmpty() ? stringValue : builder.toString().trim() + " " + stringValue);
                if (obj == null)
                    return new Pair<>(ParseResult.PARAMETER_TRANSFORMATION_FAILED, new LinkedHashMap<>());

                transformationResults.put(parameter.id, obj);
                lastValue = obj;
            }

            lastParameter = parameter;
        }

        return new Pair<>(ParseResult.SUCCESS, transformationResults);
    }

    private Pair<String, CommandExecutor<?>> findExecutor(String s) {
        Map<String, CommandExecutor<?>> map = new HashMap<>();
        subExecutors.forEach((key, value) -> {
            map.put(key, value);
            if (subExecutorsAliases.containsKey(key)) {
                for (String i : subExecutorsAliases.get(key))
                    map.put(i, value);
            }
        });

        Optional<String> executor = map.keySet().stream()
                .sorted(Comparator.comparing(String::length))
                .filter(s::startsWith)
                .findFirst();
        return executor.map(value -> new Pair<String, CommandExecutor<?>>(value, map.get(value))).orElseGet(() -> new Pair<>(null, this.executor));

    }

    public final List<String> getAliases() {
        return aliases;
    }

    public final String getDescription() {
        return description;
    }

    public enum ParseResult {
        SUCCESS, NO_PERMISSION, NO_MATCHING_EXECUTOR, NO_EXECUTORS, UNREGISTERED_CONTEXT, MISSING_PARAMETER, PARAMETER_TRANSFORMATION_FAILED, EXECUTION_CANCELLED;
    }
}