package com.autonomy.abc.selenium.util;

// hardcoded strings can be problematic
// add new languages here as necessary
public enum Language {
    CHINESE("Chinese"),
    ENGLISH("English"),
    FRENCH("French");

    private String name;

    Language(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
