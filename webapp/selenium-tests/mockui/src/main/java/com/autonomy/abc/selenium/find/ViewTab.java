package com.autonomy.abc.selenium.find;

public enum ViewTab {
    LIST("list"),
    RECOMMENDATION("recommendation"),
    TOPIC_MAP("topic-map"),
    SUNBURST("sunburst"),
    TRENDING("trending"),
    TABLE("table"),
    MAP("map");

    private final String tab;

    ViewTab(final String tab) {
        this.tab = tab;
    }

    public String css() {
        return tab;
    }
}
