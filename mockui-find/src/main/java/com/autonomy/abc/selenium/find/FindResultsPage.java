package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FindResultsPage extends AppElement implements QueryResultsPage {
    public FindResultsPage(WebDriver driver) {
        super(driver.findElement(By.className("service-view-container")), driver);
    }

    @Override
    public WebElement errorContainer() {
        return findElement(By.cssSelector(".error.well"));
    }

    public List<String> getResultTitles() {
        List<String> titles = new ArrayList<>();
        for(FindResult result : getResults()){
            titles.add(result.getTitleString());
        }
        return titles;
    }

    public WebElement resultsDiv(){
        return getDriver().findElement(By.className("results"));
    }

    public List<FindResult> getResults(){
        List<FindResult> results = new ArrayList<>();
        for(WebElement result : findElements(By.xpath("//*[starts-with(@class,'main-results-container')]"))){
            results.add(new FindResult(result, getDriver()));
        }
        return results;
    }

    public List<FindResult> getResults(int maxResults) {
        List<FindResult> results = getResults();
        return results.subList(0, Math.min(maxResults, results.size()));
    }

    public FindResult getResult(int i) {
        return new FindResult(findElement(By.cssSelector(".main-results-container:nth-of-type(" + i + ")")), getDriver());
    }

    public List<String> getDisplayedDocumentsDocumentTypes(){
        List<String> documentTypes = new ArrayList<>();
        for(FindResult result : getResults()){
            documentTypes.add(result.icon().getAttribute("class"));
        }
        return documentTypes;
    }

    public void waitForResultsToLoad() {
        Container.MIDDLE.waitForLoad(getDriver());
    }

    public FindResult searchResult(int searchResultNumber) {
        return new FindResult(findElement(By.cssSelector(".results div:nth-child(" + searchResultNumber + ")")), getDriver());
    }

    public List<WebElement> highlightedSausages(String highlightedTerm) {
        List<WebElement> highlightedRelatedTerms = findElements(new Locator()
                    .havingClass("entity-label")
                    .containingCaseInsensitive(highlightedTerm)
            );
        //on_prem
        if(!(highlightedRelatedTerms.size()>0)) {
            highlightedRelatedTerms = findHighlightedEntityText(highlightedTerm,highlightedRelatedTerms);
        }
        return highlightedRelatedTerms;
    }

    private List<WebElement> findHighlightedEntityText(String highlightedTerm, List<WebElement> highlightedRelatedTerms) {
        List<WebElement> allHighlightedTerms = findElements(new Locator()
                .havingClass("highlighted-entity-text")
                .containingCaseInsensitive(highlightedTerm)
        );

        for (WebElement sausage : allHighlightedTerms) {
            if (sausage.isDisplayed()) {
                highlightedRelatedTerms.add(sausage);
            }
        }
        return highlightedRelatedTerms;
    }


    public List<WebElement> scrollForHighlightedSausages(String highlightedTerm){
        DriverUtil.scrollToBottom(getDriver());
        waitForResultsToLoad();
        return highlightedSausages(highlightedTerm);
    }

}
