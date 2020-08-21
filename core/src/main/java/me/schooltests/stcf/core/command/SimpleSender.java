package me.schooltests.stcf.core.command;

public interface SimpleSender {
    String getName();
    String getIdentifier();
    void sendMessage(String s);
    boolean hasPermission(String s);
}
