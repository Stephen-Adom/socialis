package com.alaska.socialis.utils;

public enum NotificationTargetType {
    POST("POST"),
    COMMENT("COMMENT"),
    REPLY("REPLY"),
    USER("USER");

    private final String value;

    private NotificationTargetType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
