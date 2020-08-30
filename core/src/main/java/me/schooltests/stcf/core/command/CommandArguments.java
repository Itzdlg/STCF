package me.schooltests.stcf.core.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CommandArguments {
    private final LinkedHashMap<String, Object> argMap;
    private final ArrayList<String> order;
    public CommandArguments(LinkedHashMap<String, Object> argMap) {
        LinkedHashMap<String, Object> fixed = new LinkedHashMap<>();
        argMap.forEach((key, value) -> fixed.put(key.toLowerCase(), value));
        this.argMap = fixed;

        this.order = new ArrayList<>(fixed.keySet());
    }

    public Object get(int i) {
        return argMap.get(order.get(i));
    }

    public <T> T get(int i, Class<T> clazz) {
        try {
            return clazz.cast(get(i));
        } catch (ClassCastException e) {
            return null;
        }
    }

    public Object get(String key) {
        return argMap.get(key.toLowerCase());
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            return clazz.cast(get(key));
        } catch (ClassCastException e) {
            return null;
        }
    }
}
