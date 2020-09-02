package me.schooltests.stcf.core.command;

public class ExecutionResult {
    public final STCommand command;
    public final STCommand.ParseResult parseResult;
    public final CommandExecutor<?> executor;
    public final String alias;


    ExecutionResult(STCommand command, STCommand.ParseResult parseResult, CommandExecutor<?> executor, String alias) {
        this.command = command;
        this.parseResult = parseResult;
        this.executor = executor;
        this.alias = alias;
    }
}
