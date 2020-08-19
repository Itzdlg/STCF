package me.schooltests.stcf.core.command;

import java.util.List;

public interface CommandContext<T> {
    T transform(SimpleSender sender, Object lastValue, String value);
    List<String> tabComplete(SimpleSender sender, Object lastValue, String value);
    Class<T> getReturnClass();
}
