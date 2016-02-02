package com.autonomy.abc.selenium.page.analytics;

import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

public class AnalyticsPage extends AppElement implements AppPage {

    private AnalyticsPage(WebDriver driver) {
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

    public WebElement indexSizeChart() {
        return getDriver().findElement(By.id("index-size-flot-chart")).findElement(By.className("flot-overlay"));
    }

    public List<Container> containers() {
        return Arrays.asList(
                popularTerms(),
                zeroHitTerms(),
                promotions()
        );
    }

    public Container popularTerms() {
        return container(By.id("popularTermsListId"));
    }

    public Container zeroHitTerms() {
        return container(By.id("zeroHitTermsListId"));
    }

    public Container promotions() {
        return container(By.id("topPromotionsListId"));
    }

    private Container container(By locator) {
        WebElement container = findElement(locator);
        new WebDriverWait(getDriver(), 30).until(new WaitUntilLoadingFinished(container));
        return new Container(container);
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

    public String getPopularSearch(int i) {
        return popularTerms().get(i).getTerm();
    }

    public String getZeroHitSearch(int i) {
        return zeroHitTerms().get(i).getTerm();
    }

    public String getMostPopularNonZeroSearchTerm() {
        for (ContainerItem item : popularTerms()) {
            if (!isZeroHitTerm(item.getTerm())) {
                return item.getTerm();
            }
        }

        throw new NoSuchElementException("All popular search terms are zero hit terms");
    }

    private boolean isZeroHitTerm(String term) {
        for (ContainerItem item : zeroHitTerms()) {
            if (item.getTerm().equalsIgnoreCase(term)) {
                return true;
            }
        }

        return false;
    }

    public static class Factory implements ParametrizedFactory<WebDriver, AnalyticsPage> {
        @Override
        public AnalyticsPage create(WebDriver context) {
            return new AnalyticsPage(context);
        }
    }
}
