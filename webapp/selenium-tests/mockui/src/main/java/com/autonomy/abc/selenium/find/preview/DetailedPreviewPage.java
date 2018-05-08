package com.autonomy.abc.selenium.find.preview;

import com.hp.autonomy.frontend.selenium.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DetailedPreviewPage extends AppElement implements AppPage {

    private DetailedPreviewPage(final WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(DetailedPreviewPage::loadWaitPredicate), driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(DetailedPreviewPage::loadWaitPredicate);
    }

    public void openOriginalDoc() {
        findElement(By.cssSelector(".document-detail-service-view-container .document-detail-open-original-link")).click();
        Waits.loadOrFadeWait();
    }

    public String originalDocLink() {
        return findElement(By.className("document-detail-open-original-link")).getAttribute("href");
    }

    public void goBackToSearch() {
        findElement(By.className("detail-view-back-button")).click();
    }

    public WebElement tabLoadingIndicator() {
        return findElement(By.cssSelector(".tab-content-view-container.active .loading-spinner"));
    }

    public void waitForTabToLoad() {
        new WebDriverWait(getDriver(), 5)
                .until(driver -> !tabLoadingIndicator().isDisplayed());
    }

    public WebElement frame() {
        return findElement(By.tagName("iframe"));
    }

    public boolean frameExists() {
        return !findElements(By.tagName("iframe")).isEmpty();
    }

    public WebElement similarDatesTab() {
        return findElement(By.xpath("//span[contains(text(),'Similar dates')]"));
    }

    public WebElement similarDocsTab() {
        return findElement(By.xpath("//span[contains(text(),'Similar documents')]"));
    }

    public boolean locationTabExists() {
        final By locator = By.xpath("//ul[contains(@class,'document-detail-tabs')]//span[contains(text(),'Location')]");
        return !findElements(locator).isEmpty();
    }

    public WebElement ithTick(final int i) {
        final String tickPercent = String.valueOf((i - 1) * 10);
        return findElement(By.xpath("//div[contains(@class,'slider-tick') and contains(@style,'left: " + tickPercent + "%')]"));
    }

    public String getSimilarDatesSummary() {
        return findElement(By.className("similar-dates-summary")).getText();
    }

    public int numberOfHeadersWithDocTitle() {
        final String title = getTitle();
        return findElements(By.xpath("//h1[contains(text(),'" + title + "')]")).size();
    }

    public String getTitle() {
        return getField("Title");
    }

    public String getReference() {
        return getField("Reference");
    }

    public String getIndex() {
        return getField("Index");
    }

    public String getDatabase() {
        return getField("Database");
    }

    public String getSummary() {
        return getField("Summary");
    }

    public String getDate() {
        return getField("Date");
    }

    public String getAuthor() {
        return getField("Authors");
    }

    private String getField(final String name) {
        try {
            return findElement(By.xpath(".//th[contains(text(), '" + name + "')]/following::td")).getText();
        } catch (final NoSuchElementException ignored) {
            return null;
        }
    }

    private static WebElement loadWaitPredicate(final SearchContext driver) {
        final WebElement serviceViewContainer = driver.findElement(By.cssSelector(".document-detail-service-view-container"));
        final WebElement loadingElement = serviceViewContainer.findElement(By.cssSelector(".document-content-loading"));
        return ElementUtil.hasClass("hide", loadingElement) ? serviceViewContainer : null;
    }

    public static class Factory implements ParametrizedFactory<WebDriver, DetailedPreviewPage> {
        @Override
        public DetailedPreviewPage create(final WebDriver context) {
            return new DetailedPreviewPage(context);
        }
    }
}
