package com.autonomy.abc.selenium.page.analytics;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class AnalyticsPage extends AppElement implements AppPage {

    public AnalyticsPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),45).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return driver.findElements(By.className("loadingIconSmall")).size() == 0;
            }
        });
    }

    private WebElement getPopularTermContainer(){
        return findElement(By.id("popularTermsListId"));
    }

    private WebElement getPromotionsContainer(){
        return findElement(By.id("topPromotionsListId"));
    }

    private WebElement getZeroHitContainer(){
        return findElement(By.id("zeroHitTermsListId"));
    }

    public WebElement getMostPopularPromotion() {
        WebElement container = getPromotionsContainer();
        new WebDriverWait(getDriver(),30).until(new WaitUntilLoadingFinished(container));

        return container.findElement(By.cssSelector(".list-group-item:nth-child(1) a"));
    }

    public Term getMostPopularZeroSearchTerm() {
        WebElement container = getZeroHitContainer();
        new WebDriverWait(getDriver(),30).until(new WaitUntilLoadingFinished(container));
        WebElement topTerm = container.findElement(By.cssSelector(".list-group-item:nth-child(1)"));

        return new Term(topTerm);
    }

    public Term getMostPopularNonZeroSearchTerm() {
        for(WebElement term : getPopularTerms()){
            if(!zeroHitTerm(term.findElement(By.tagName("a")).getText())){
                return new Term(term);
            }
        }
        throw new NoSuchElementException("All popular search terms are zero hit terms");
    }

    private boolean zeroHitTerm(String term) {
        for(WebElement zeroTerm : getZeroHitContainer().findElements(By.cssSelector(".list-group-item a"))){
            if (zeroTerm.getText().equals(term)){
                return true;
            }
        }

        return false;
    }

    private List<WebElement> getPopularTerms() {
        return getPopularTermContainer().findElements(By.className("list-group-item"));
    }

    public void reversePromotionSort() {
        reverseSort(getPromotionsContainer());
    }

    private void reverseSort(WebElement container){
        container.findElement(By.xpath(".//*[contains(text(),'Count')]")).click();
    }

    public WebElement indexSizeChart() {
        return getDriver().findElement(By.id("index-size-flot-chart")).findElement(By.className("flot-overlay"));
    }

    private class WaitUntilLoadingFinished implements ExpectedCondition<Boolean> {
        WebElement container;

        private WaitUntilLoadingFinished(WebElement container) {
            this.container = container;
        }

        @Override
        public Boolean apply(WebDriver input) {
            return container.findElements(By.className("loadingIconSmall")).isEmpty();
        }
    }

    public Term getMostPopularSearchTerm() {
        return getPopularSearchTerm(1);
    }

    public Term getPopularSearchTerm(int num) {
        WebElement container = getPopularTermContainer();
        new WebDriverWait(getDriver(),30).until(new WaitUntilLoadingFinished(container));
        WebElement topTerm = container.findElement(By.cssSelector(".list-group-item:nth-child(" + num + ")"));

        return new Term(topTerm);
    }

}
