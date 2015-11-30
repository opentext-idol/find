package com.autonomy.abc.selenium.language;

// hardcoded strings can be problematic
// add new languages here as necessary
public enum Language {
    ARABIC("Arabic"),
    CHINESE("Chinese"),
    ENGLISH("English"),
    FRENCH("French"),
    SWAHILI("Swahili");

    private String name;

    Language(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
