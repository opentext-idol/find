package com.autonomy.abc.selenium.keywords;

public enum KeywordType {
    BLACKLIST("Blacklist", "Blacklisted Terms", "Select terms to blacklist"),
    SYNONYMS("Synonyms", "Synonyms", "Select synonyms");

    private String name;
    private String optionTitle;
    private String inputTitle;

    KeywordType(String name, String optionTitle, String inputTitle) {
        this.name = name;
        this.optionTitle = optionTitle;
        this.inputTitle = inputTitle;
    }

    String getInputTitle() {
        return inputTitle;
    }

    String getOptionTitle() {
        return optionTitle;
    }

    @Override
    public String toString() {
        return name;
    }
}
