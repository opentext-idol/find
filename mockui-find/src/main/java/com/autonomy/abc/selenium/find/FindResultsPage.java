package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class FindResultsPage extends AppElement implements QueryResultsPage {
    public FindResultsPage(WebDriver driver) {
        super(driver.findElement(By.className("service-view-container")), driver);
    }

    public List<WebElement> relatedConcepts() {
        waitForSearchLoadIndicatorToDisappear(Container.RIGHT);
        return findElements(By.cssSelector(".related-concepts-list a"));
    }

    public List<String> getRelatedConcepts() {
        return ElementUtil.getTexts(relatedConcepts());
    }

    public WebElement hoverOverRelatedConcept(int i) {
        WebElement concept = relatedConcepts().get(i);
        DriverUtil.hover(getDriver(), concept);
        WebElement popover = findElement(By.className("popover"));
        waitForPopoverToLoad(popover);
        return popover;
    }

    public void unhover() {
        /* click somewhere not important to remove hover -
        * clicking the search term box seems safe... */
        getDriver().findElement(By.cssSelector("input.find-input")).click();
        new WebDriverWait(getDriver(),2).until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("popover"))));
    }

    private void waitForPopoverToLoad(WebElement popover) {
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(popover, "Loading")));
    }

    @Override
    public WebElement errorContainer() {
        return findElement(By.cssSelector(".error.well"));
    }

    public List<FindResult> promotions() {
        List<FindResult> results = new ArrayList<>();
        for(WebElement result : promotionsDiv().findElements(By.className("promoted-document"))) {
            results.add(new FindResult(result, getDriver()));
        }
        return results;
    }

    public List<String> getPromotionsTitles(){
        List<String> titles = new ArrayList<>();
        for(FindResult promotion : promotions()){
            titles.add(promotion.getTitleString());
        }
        return titles;
    }

    private WebElement promotionsDiv(){
        return findElement(By.className("promotions"));
    }

    public List<String> getResultTitles() {
        List<String> titles = new ArrayList<>();
        for(FindResult result : getResults()){
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

    public WebElement popover() {
        return findElement(By.className("popover"));
    }

    public int getResultsCount() {
        return getResultTitles().size();
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

    public List<FindResult> getResults(){
        List<FindResult> results = new ArrayList<>();
        for(WebElement result : findElements(By.cssSelector("[data-rel='results']"))){
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

    public enum Container{
        LEFT("left-side"),
        MIDDLE("middle"),
        RIGHT("right-side");

        private final String container;

        Container(String container) {
            this.container = container;
        }

        private String asCssClass() {
            return "." + container + "-container";
        }
    }

    public void waitForSearchLoadIndicatorToDisappear(final Container container){
        try {
            new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(container.asCssClass() + " .loading-spinner")));
        } catch (Exception e) {
            //Noop
        }

        new WebDriverWait(getDriver(), 60)
                .withMessage("Container " + container + " failed to load")
                .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(container.asCssClass() + " .fa-spinner")));
    }

    public FindResult searchResult(int searchResultNumber) {
        return new FindResult(findElement(By.cssSelector(".results div:nth-child(" + searchResultNumber + ")")), getDriver());
    }

    public WebElement highlightRelatedConceptsButton() {
        return findElement(By.className("highlight-related-concepts"));
    }

    public List<WebElement> highlightedSausages(String highlightedTerm) {
        return findElements(new Locator()
                        .havingClass("entity-label")
                        .containingCaseInsensitive(highlightedTerm)
        );
    }
}
