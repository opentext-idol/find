package com.autonomy.abc.selenium.find.results;

import com.autonomy.abc.selenium.find.Container;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FindResultsPage extends AppElement implements QueryResultsPage {
    public FindResultsPage(final WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public FindResultsPage(final WebDriver driver) {
        this(Container.currentTabContents(driver).findElement(By.className("middle-container")), driver);
    }

    public int getResultsCount() {
        return Integer.valueOf(findElement(By.className("total-results-number")).getText());
    }

    @Override
    public WebElement errorContainer() {
        return findElement(By.cssSelector(".error.well"));
    }

    public List<String> getResultTitles() {
        final List<String> titles = new ArrayList<>();
        for (final FindResult result : getResults()) {
            titles.add(result.getTitleString());
        }
        return titles;
    }

    public WebElement resultsDiv() {
        return getDriver().findElement(By.className("results"));
    }

    public boolean loadingIndicatorPresent(){
        return findElements(By.cssSelector(".main-results-content .loading-spinner")).size()>0;
    }

    public List<FindResult> getResults() {
        final List<FindResult> results = new ArrayList<>();
        for (final WebElement result : findElements(By.className("main-results-container"))) {
            results.add(new FindResult(result, getDriver()));
        }
        return results;
    }

    public List<FindResult> getResults(final int maxResults) {
        final List<FindResult> results = getResults();
        return results.subList(0, Math.min(maxResults, results.size()));
    }

    public FindResult getResult(final int i) {
        return new FindResult(findElement(By.cssSelector(".main-results-container:nth-of-type(" + i + ')')), getDriver());
    }

    public List<String> getDisplayedDocumentsDocumentTypes() {
        final List<String> documentTypes = new ArrayList<>();
        for (final FindResult result : getResults()) {
            documentTypes.add(result.icon().getAttribute("class"));
        }
        return documentTypes;
    }

    public void waitForResultsToLoad() {
        Container.MIDDLE.waitForLoad(getDriver());
    }

    public FindResult searchResult(final int searchResultNumber) {
        return new FindResult(findElement(By.cssSelector(".results div:nth-child(" + searchResultNumber + ')')), getDriver());
    }

    public boolean mainResultsContainerHidden() {
        return !findElement(By.className("main-results-content-container")).isDisplayed();
    }

    public List<WebElement> scrollForHighlightedSausages() {
        DriverUtil.scrollToBottom(getDriver());
        waitForResultsToLoad();

        final Locator sausageLocator = new Locator()
                .havingClass("highlighted-entity-text");

        final List<WebElement> visibleRelatedTerms = new LinkedList<>();
        final List<WebElement> allHighlightedTerms = findElements(sausageLocator);

        for (final WebElement sausage : allHighlightedTerms) {
            if (sausage.isDisplayed()) {
                visibleRelatedTerms.add(sausage);
            }
        }

        return visibleRelatedTerms;
    }
}
