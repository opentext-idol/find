package com.autonomy.abc.selenium.find.comparison;


import org.openqa.selenium.By;

public enum AppearsInTopicMap {
    FIRST_SEARCH("first"),
    BOTH("both"),
    SECOND_SEARCH("second");

    private final By mapLocator;

    AppearsInTopicMap(final String position) {
        mapLocator = By.cssSelector(".topic-map-comparison-selection a[data-tab-id='" + position+ "']");
    }

    By mapLocator() {
        return mapLocator;
    }
}
