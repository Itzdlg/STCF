package me.schooltests.stcf.core.command;

@SuppressWarnings({"WeakerAccess"})
public class CommandParameter {
    public final String id;
    public final Class contextClass;
    public final boolean required;

    public CommandParameter(String id, Class contextClass, boolean required) {
        this.id = id;
        this.contextClass = contextClass;
        this.required = required;
    }
}