package com.autonomy.abc.selenium.keywords;

public enum KeywordWizardType {
    BLACKLIST("BLACKLISTED", "Select terms to blacklist"),
    SYNONYMS("SYNONYMS", "Select synonyms");

    private final String option;
    private final String inputTitle;

    KeywordWizardType(String option, String inputTitle) {
        this.option = option;
        this.inputTitle = inputTitle;
    }

    String getInputTitle() {
        return inputTitle;
    }

    String getOption() {
        return option;
    }
}
