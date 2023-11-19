package com.alaska.socialis.utils;

public enum NotificationActivityType {
    LIKED("LIKED"),
    COMMENTED("COMMENTED"),
    REPLY("REPLY"),
    FOLLOWS("FOLLOWS"),
    FRIEND_REQUEST("FRIEND_REQUEST"),
    MENTION("MENTION"),
    MESSAGE("MESSAGE");

    private final String value;

    private NotificationActivityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
