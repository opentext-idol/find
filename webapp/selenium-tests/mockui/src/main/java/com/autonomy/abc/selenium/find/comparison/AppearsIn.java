package com.autonomy.abc.selenium.find.comparison;

import org.openqa.selenium.By;

public enum AppearsIn {
    THIS_ONLY("left"),
    BOTH("middle"),
    OTHER_ONLY("right");

    private final By resultsListLocator;

    AppearsIn(final String resultsContainerPosition) {
        resultsListLocator = By.className("comparison-results-view-container-" + resultsContainerPosition);
    }

    By getResultsListLocator() {
        return resultsListLocator;
    }
}
