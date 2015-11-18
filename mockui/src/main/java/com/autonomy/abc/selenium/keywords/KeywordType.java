package com.autonomy.abc.selenium.keywords;

public enum KeywordType {
    BLACKLIST("Blacklist", "BLACKLISTED", "Select terms to blacklist"),
    SYNONYMS("Synonyms", "SYNONYMS", "Select synonyms");

    private String name;
    private String option;
    private String inputTitle;

    KeywordType(String name, String option, String inputTitle) {
        this.name = name;
        this.option = option;
        this.inputTitle = inputTitle;
    }

    String getInputTitle() {
        return inputTitle;
    }

    String getOption() {
        return option;
    }

    @Override
    public String toString() {
        return name;
    }
}
