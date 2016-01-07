package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

public class FindResultsPage extends AppElement {
    public FindResultsPage(WebDriver driver) {
        super(driver.findElement(By.className("service-view-container")), driver);
    }

    public WebElement getRelatedConcepts() {
        return findElement(By.className("related-concepts-list"));
    }

    public List<WebElement> getPromotions() {
        return getPromotionsDiv().findElements(By.className("promoted-document"));
    }

    public List<String> getPromotionsTitles(){
        List<String> titles = new ArrayList<>();
        for(WebElement promotion : getPromotions()){
            titles.add(promotion.findElement(By.tagName("h4")).getText());
        }
        return titles;
    }

    private WebElement getPromotionsDiv(){
        return findElement(By.className("promotions"));
    }

    public void filterByDate(DateEnum date) {
        findElement(By.cssSelector("td[data-id='" + date.toString().toLowerCase() + "']")).click();
        waitForSearchLoadIndicatorToDisappear(Container.MIDDLE);
    }

    public List<String> getResultTitles() {
        List<String> titles = new ArrayList<>();
        for(WebElement result : getResults()){
            titles.add(result.findElement(By.tagName("h4")).getText());
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

    public void filterByParametric(String header, String filter) {
        findElement(By.cssSelector("[data-field='"+header.toLowerCase().replace(" ","_")+"'] [data-value='"+filter.toUpperCase()+"']"));
    }

    public List<WebElement> getSimilarResultLinks() {
        return findElements(By.className("similar-documents-trigger"));
    }

    public WebElement getPopover() {
        return findElement(By.className("popover"));
    }

    public enum DateEnum {
        WEEK,
        MONTH,
        YEAR,
        CUSTOM
    }

    private enum ParametricContainers {
        AUTHOR("author"),
        CONTENTTYPE("content_type");

        private final String dataField;

        ParametricContainers(String dataField){
            this.dataField = dataField;
        }

        public String getDataField(){
            return dataField;
        }
    }

    private WebElement getParametricContainer(ParametricContainers param) {
        return getParametricContainer(param.dataField);
    }

    public WebElement getParametricContainer(String param){
        return findElement(By.className("parametric-container")).findElement(By.cssSelector("[data-field='" + param + "']"));
    }

    private WebElement getContentTypeContainer(){
        return getParametricContainer(ParametricContainers.CONTENTTYPE);
    }

    public void selectContentType(String contentType){
        waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.LEFT);
        getContentTypeContainer().findElement(By.cssSelector("[data-value='" + contentType.toUpperCase() + "']")).click();
        waitForSearchLoadIndicatorToDisappear(Container.MIDDLE);
    }

    public WebElement getResultsDiv(){
        return getDriver().findElement(By.className("results"));
    }

    public List<WebElement> getResults(){
        return getResultsDiv().findElements(By.cssSelector("[data-rel='results']"));
    }

    public List<String> getDisplayedDocumentsDocumentTypes(){
        List<String> documentTypes = new ArrayList<String>();
        for(WebElement result : getResults()){
            documentTypes.add(result.findElement(By.cssSelector(".content-type i")).getAttribute("class"));
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

    public WebElement getSearchResult(int searchResultNumber) {
        return findElement(By.cssSelector(".results div:nth-child(" + searchResultNumber + ")"));
    }

    public WebElement getSearchResultTitle(int searchResultNumber) {
        return getSearchResult(searchResultNumber).findElement(By.tagName("h4"));
    }
}
