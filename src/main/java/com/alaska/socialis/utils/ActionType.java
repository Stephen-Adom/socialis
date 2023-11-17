package com.alaska.socialis.utils;

public enum ActionType {
    NEW_POST("NEW_POST"),
    ADD_COMMENT("ADD_COMMENT"),
    ADD_REPLY("ADD_REPLY"),
    FOLLOW_USER("FOLLOW_USER"),
    LIKE_POST("LIKE_POST"),
    LIKE_COMMENT("LIKE_COMMENT"),
    LIKE_REPLY("LIKE_REPLY"),
    BOOKMARK_POST("BOOKMARK_POST");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
