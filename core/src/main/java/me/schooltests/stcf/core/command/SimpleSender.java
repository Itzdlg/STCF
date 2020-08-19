package me.schooltests.stcf.core.command;

public interface SimpleSender {
    String getName();
    void sendMessage(String s);
    boolean hasPermission(String s);
}
