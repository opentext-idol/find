package com.autonomy.abc.selenium.query;

public enum SortBy {
    DATE("by date"),
    RELEVANCE("by relevance");

    private final String name;

    SortBy(final String content) {
        name = content;
    }

    @Override
    public String toString() {
        return name;
    }
}
