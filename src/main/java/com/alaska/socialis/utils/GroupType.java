package com.alaska.socialis.utils;

public enum GroupType {
    POST("POST"),
    COMMENT("COMMENT"),
    REPLY("REPLY"),
    USER("USER");

    private final String value;

    GroupType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
