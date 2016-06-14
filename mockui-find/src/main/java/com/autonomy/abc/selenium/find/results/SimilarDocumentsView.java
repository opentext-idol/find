package com.autonomy.abc.selenium.find.results;

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
    private final WebDriver driver;
    private final WebElement container;

    //TODO find somewhere more suitable for this
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMMMM dd kk:mm:ss zzz yyyy");

    private SimilarDocumentsView(final WebDriver driver) {
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
        return !findElements(By.cssSelector(".results-message-container h4")).isEmpty();
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
        final List<FindResult> results = new ArrayList<>();
        for(final WebElement result : findElements(By.className("main-results-container"))){
            results.add(new FindResult(result, getDriver()));
        }
        return results;
    }

    public List<FindResult> getResults(final int maxResults) {
        final List<FindResult> results = getResults();
        return results.subList(0, Math.min(maxResults, results.size()));
    }

    public FindResult getResult(final int i) {
        return new FindResult(findElement(By.cssSelector(".main-results-container:nth-of-type(" + i + ")")), getDriver());
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 20)
                .withMessage("loading similar results view")
                .until(ExpectedConditions.visibilityOf(backButton()));
    }

    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }

    private WebDriver getDriver() {
        return driver;
    }

    public WebElement mainResultsContent() {
        return findElement(By.className("main-results-content"));
    }

    public void sortByDate() {
        sortBy(1);
    }

    public void sortByRelevance() {
        sortBy(2);
    }

    public String convertDate(final String badFormatDate){
        final String[] words = badFormatDate.split(" ");
        final int timeAmount;
        final String timeUnit;
        if(words[0].equals("a")||words[0].equals("an")){
            timeAmount=1;
            timeUnit = words[1];
        }
        else{
            timeAmount= Integer.parseInt(words[0]);
            timeUnit = words[1];
        }

        final Calendar date = Calendar.getInstance();

        switch (timeUnit) {
            case "minute":
            case "minutes":
                date.add(Calendar.MINUTE,-timeAmount);
                break;

            case "hour":
            case "hours":
                date.add(Calendar.HOUR_OF_DAY, -timeAmount);
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
        date.set(Calendar.SECOND,0);
        return date.getTime().toString();
    }


    private void sortBy(final int dropdownRow){
        findElement(By.className("current-search-sort")).click();
        findElement(By.cssSelector(".search-results-sort li:nth-child(" + dropdownRow + ")")).click();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, SimilarDocumentsView> {
        public SimilarDocumentsView create(final WebDriver context) {
            return new SimilarDocumentsView(context);
        }
    }
}
