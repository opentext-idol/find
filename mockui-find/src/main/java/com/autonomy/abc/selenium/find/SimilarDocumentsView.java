package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SimilarDocumentsView implements AppPage {
    private WebDriver driver;
    private WebElement container;

    //TODO find somewhere more suitable for this
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, MMMMM dd, yyyy kk:mm a");

    private SimilarDocumentsView(WebDriver driver) {
        this.driver = driver;
        this.container = driver.findElement(By.className("suggest-service-view-container"));
    }

    public WebElement backButton() {
        return findElement(By.xpath("//button[contains(text(),'Back')]"));
    }

    public WebElement seedLink() {
        return title().findElement(By.tagName("a"));
    }

    private WebElement title() {
        if (resultsMessageContainerExists()){
            return findElement(By.cssSelector(".results-message-container h4"));
        }
        return findElement(By.tagName("h1"));
    }

    private Boolean resultsMessageContainerExists(){
        return findElements(By.cssSelector(".results-message-container h4")).size()>0;
    }

    public String getTitle() {
        return title().getText();
    }

    public WebElement loadingIndicator(){
            return findElement(By.className("view-server-loading-indicator"));
    }

    public WebElement previewContents(){
        return findElement(By.className("preview-mode-contents"));
    }

    /**
     * Y in 'X to Y of Z'
     */
    public int getVisibleResultsCount() {
        return Integer.valueOf(findElement(By.className("current-results-number")).getText());
    }

    /**
     * Z in 'X to Y of Z'
     */
    public int getTotalResults() {
        return Integer.valueOf(findElement(By.className("total-results-number")).getText());
    }

    public List<FindResult> getResults(){
        List<FindResult> results = new ArrayList<>();
        for(WebElement result : findElements(By.className("main-results-container"))){
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

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 20)
                .withMessage("loading similar results view")
                .until(ExpectedConditions.visibilityOf(backButton()));
    }

    private WebElement findElement(By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(By locator) {
        return container.findElements(locator);
    }

    private WebDriver getDriver() {
        return driver;
    }

    public WebElement mainResultsContent() {
        return findElement(By.className("main-results-content"));
    }

    public boolean publicIndexesExist(){
        return findElements(By.xpath("//*[contains(text(),'Public Indexes')]")).size()>0;
    }
    public void sortByDate() {
        sortBy(1);
    }

    public void sortByRelevance() {
        sortBy(2);
    }

    //want to be able to parse the date format 'x hours/days/months/years ago' -> how date is
    //in detailed preview and findPage and similarDocumentPage
    public String convertDate(String badFormatDate){
        int timeAmount= Integer.parseInt(badFormatDate.split("")[0]);
        String timeUnit = badFormatDate.split("")[1];

        Calendar date = Calendar.getInstance();

        switch (timeUnit) {
            case "hour":
            case "hours":
                date.add(Calendar.HOUR, -timeAmount);
                break;

            case "day":
            case "days":
                date.add(Calendar.DAY_OF_MONTH,-timeAmount);
                break;

            case "month":
            case "months":
                date.add(Calendar.MONTH,-timeAmount);
                break;

            case "year":
            case "years":
                date.add(Calendar.YEAR,-timeAmount);
                break;
        }
        //so messy -> doing this way need to getTime () (Date date) then getString then convert back.
        return date.toString();
    }


    private void sortBy(int dropdownRow){
        findElement(By.className("current-search-sort")).click();
        findElement(By.cssSelector(".search-results-sort li:nth-child(" + dropdownRow + ")")).click();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, SimilarDocumentsView> {
        public SimilarDocumentsView create(WebDriver context) {
            return new SimilarDocumentsView(context);
        }
    }
}
