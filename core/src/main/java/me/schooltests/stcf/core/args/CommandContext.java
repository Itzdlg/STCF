package me.schooltests.stcf.core.args;

import me.schooltests.stcf.core.command.SimpleSender;

import java.util.List;

public interface CommandContext<T> {
    T transform(SimpleSender sender, Object lastValue, String value);
    List<String> tabComplete(SimpleSender sender, Object lastValue, String value);
    Class<T> getReturnClass();
}
