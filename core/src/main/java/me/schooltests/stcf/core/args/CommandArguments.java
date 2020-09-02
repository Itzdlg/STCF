package me.schooltests.stcf.core.args;

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
        if (i >= order.size())
            return order.get(order.size() - 1);

        return argMap.get(order.get(i));
    }

    public <T> T get(int i, Class<T> clazz) {
        if (get(i) == null) return null;

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
        if (get(key) == null) return null;

        try {
            return clazz.cast(get(key));
        } catch (ClassCastException e) {
            return null;
        }
    }
}
