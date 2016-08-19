package com.autonomy.abc.selenium.keywords;

public enum KeywordFilter {
    ALL("All Types"),
    BLACKLIST("Blacklist"),
    SYNONYMS("Synonyms");

    private final String filterName;

    KeywordFilter(final String name) {
        filterName = name;
    }

    public String toString() {
        return filterName;
    }

}
