/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.selenium.find.results;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.stream.Collectors;

public class ListView extends AppElement implements QueryResultsPage {
    private static final int RESULTS_TIMEOUT_SECONDS = 50;

    public ListView(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    public ListView(final WebDriver driver) {
        this(ElementUtil.ancestor(Container.currentTabContents(driver).findElement(By.className("main-results-content-container")), 1), driver);
    }

    public int getTotalResultsNum() {
        return Integer.parseInt(findElement(By.className("total-results-number")).getText());
    }

    public List<WebElement> resultsContainingString(final String searchTerm) {
        return getDriver().findElements(By.xpath("//*[contains(@class,'search-text') and contains(text(),'" + searchTerm + "')]"));
    }

    @Override
    public WebElement errorContainer() {
        return findElement(By.cssSelector(".well"));
    }

    public boolean errorContainerShown() {
        final List<WebElement> errorWells = findElements(By.cssSelector(".well"));
        return !errorWells.isEmpty() && errorWells.get(0).isDisplayed();
    }

    public List<String> getResultTitles() {
        return getResults().stream()
                .map(FindResult::getTitleString)
                .collect(Collectors.toList());
    }

    public List<String> getResultsReferences() {
        return getResults().stream()
                .map(FindResult::getReference)
                .collect(Collectors.toList());
    }

    public WebElement resultsDiv() {
        return getDriver().findElement(By.className("results"));
    }

    public boolean loadingIndicatorPresent() {
        return !findElements(By.cssSelector(".main-results-content .loading-spinner:not(.hide)")).isEmpty();
    }

    public List<FindResult> getResults() {
        return findElements(By.className("main-results-container")).stream()
                .map(resultElement -> new FindResult(resultElement, getDriver()))
                .collect(Collectors.toList());
    }

    public List<FindResult> getResults(final int maxResults) {
        final List<FindResult> results = getResults();
        return results.subList(0, Math.min(maxResults, results.size()));
    }

    public FindResult getResult(final int i) {
        return new FindResult(findElement(By.cssSelector(".main-results-list.results .main-results-container:nth-of-type(" + i + ')')), getDriver());
    }

    public List<String> getDisplayedDocumentsDocumentTypes() {
        return getResults().stream()
                .map(result -> result.icon().getAttribute("class"))
                .collect(Collectors.toList());
    }

    public void waitForResultsToLoad() {
        new WebDriverWait(getDriver(), RESULTS_TIMEOUT_SECONDS).until(driver -> {
            if (loadingIndicatorPresent()) {
                return false;
            } else if (errorContainerShown() || !findElements(By.cssSelector(".result-message:not(.hide)")).isEmpty() || !getResults().isEmpty()) {
                return true;
            } else {
                throw new IllegalStateException("No loading indicator, empty message, error message or result list present");
            }
        });
    }

    public FindResult searchResult(final int searchResultNumber) {
        return new FindResult(findElement(By.cssSelector(".results div:nth-child(" + searchResultNumber + ')')), getDriver());
    }
}
