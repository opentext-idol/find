package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.FindParametricCheckbox;
import com.autonomy.abc.selenium.search.FindSearchResult;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class FindResultsPage extends AppElement {
    public FindResultsPage(WebDriver driver) {
        super(driver.findElement(By.className("service-view-container")), driver);
    }

    public List<WebElement> relatedConcepts() {
        waitForSearchLoadIndicatorToDisappear(Container.RIGHT);
        return findElements(By.cssSelector(".related-concepts-list a"));
    }

    public WebElement hoverOverRelatedConcept(int i) {
        WebElement concept = relatedConcepts().get(i);
        ElementUtil.hover(concept, getDriver());
        WebElement popover = findElement(By.className("popover"));
        waitForPopoverToLoad(popover);
        return popover;
    }

    public void unhover() {
        /* click somewhere not important to remove hover -
        * clicking the search term box seems safe... */
        getDriver().findElement(By.cssSelector("input[name='find-input']")).click();
        new WebDriverWait(getDriver(),2).until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("popover"))));
    }

    private void waitForPopoverToLoad(WebElement popover) {
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));
    }

    public List<FindSearchResult> promotions() {
        List<FindSearchResult> results = new ArrayList<>();
        for(WebElement result : promotionsDiv().findElements(By.className("promoted-document"))) {
            results.add(new FindSearchResult(result));
        }
        return results;
    }

    public List<String> getPromotionsTitles(){
        List<String> titles = new ArrayList<>();
        for(FindSearchResult promotion : promotions()){
            titles.add(promotion.getTitleString());
        }
        return titles;
    }

    private WebElement promotionsDiv(){
        return findElement(By.className("promotions"));
    }

    public List<String> getResultTitles() {
        List<String> titles = new ArrayList<>();
        for(FindSearchResult result : getResults()){
            titles.add(result.getTitleString());
        }
        return titles;
    }

    public List<String> getResultTitles(int start, int end){
        List<String> titles = new ArrayList<>();

        for(int i = start; i <= end; i++){
            titles.add(findElement(By.cssSelector("[data-rel=results]:nth-child("+i+"):not(.promoted-document) h4")).getText());
        }

        return titles;
    }

    public List<WebElement> similarResultLinks() {
        return findElements(By.className("similar-documents-trigger"));
    }

    public WebElement popover() {
        return findElement(By.className("popover"));
    }

    public enum DateEnum {
        WEEK,
        MONTH,
        YEAR,
        CUSTOM
    }

    public void toggleDateSelection(DateEnum date) {
        dateOption(date).click();
        waitForSearchLoadIndicatorToDisappear(Container.MIDDLE);
    }

    public boolean isDateSelected(DateEnum date) {
        WebElement checkIcon = dateOption(date).findElement(By.tagName("i"));
        return !ElementUtil.hasClass("hide", checkIcon);
    }

    private WebElement dateOption(DateEnum date) {
        return findElement(By.cssSelector("tr[data-id='" + date.toString().toLowerCase() + "']"));
    }

    public WebElement parametricContainer(String param){
        return findElement(By.className("parametric-container")).findElement(By.cssSelector("[data-field='" + param + "']"));
    }

    public FindParametricCheckbox parametricTypeCheckbox(String category, String field){
        WebElement checkbox = findElement(By.cssSelector("[data-field='" + category.toLowerCase().replace(" ","_") + "'] [data-value='" + field.toUpperCase() + "']"));
        return new FindParametricCheckbox(checkbox, getDriver());
    }

    public WebElement resultsDiv(){
        return getDriver().findElement(By.className("results"));
    }

    public List<FindSearchResult> getResults(){
        List<FindSearchResult> results = new ArrayList<>();
        for(WebElement result : findElements(By.cssSelector("[data-rel='results']"))){
            results.add(new FindSearchResult(result));
        }
        return results;
    }

    public List<FindSearchResult> getResults(int maxResults) {
        List<FindSearchResult> results = getResults();
        return results.subList(0, Math.min(maxResults, results.size()));
    }

    public List<String> getDisplayedDocumentsDocumentTypes(){
        List<String> documentTypes = new ArrayList<String>();
        for(FindSearchResult result : getResults()){
            documentTypes.add(result.getIcon().getAttribute("class"));
        }
        return documentTypes;
    }

    public enum Container{
        LEFT("left-side"),
        MIDDLE("middle"),
        RIGHT("right-side");

        private final String container;

        Container(String container) {
            this.container = container;
        }

        public String getContainer() {
            return container;
        }
    }

    public void waitForSearchLoadIndicatorToDisappear(final Container container){
        try {
            new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("." + container.getContainer() + "-container .loading-spinner")));
        } catch (Exception e) {
            //Noop
        }

        new WebDriverWait(getDriver(), 60).withMessage("Container " + container + " failed to load").until(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver driver) {
                List<WebElement> indicators = findElement(By.className(container.getContainer() + "-container")).findElements(By.className("loading-spinner"));

                if (indicators.size() == 0) {
                    return true;
                }

                for (WebElement indicator : indicators) {
                    if (container == Container.RIGHT) {
                        if (!indicator.findElement(By.xpath("..")).getAttribute("class").contains("hide")) {
                            return false;
                        }
                    } else if (!indicator.getAttribute("class").contains("hide")) {
                        return false;
                    }
                }

                return true;
            }
        });
    }

    public FindSearchResult searchResult(int searchResultNumber) {
        return new FindSearchResult(findElement(By.cssSelector(".results div:nth-child(" + searchResultNumber + ")")));
    }
}
