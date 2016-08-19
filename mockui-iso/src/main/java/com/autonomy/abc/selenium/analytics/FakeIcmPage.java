package com.autonomy.abc.selenium.analytics;

import com.autonomy.abc.selenium.application.AppPageFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * used to simulate switching to ICM, but any test
 * that actually needs to interact with ICM should
 * be in with the HSOD tests
 */
public class FakeIcmPage extends DashboardBase {
    public FakeIcmPage(final WebDriver driver) {
        super(waitForLoad(driver), driver);
    }

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    private static WebElement waitForLoad(final WebDriver driver) {
        return new WebDriverWait(driver, 30)
                .withMessage("switching to ICM")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content")));
    }

    public static class Factory implements AppPageFactory<FakeIcmPage> {
        @Override
        public Class<FakeIcmPage> getPageType() {
            return FakeIcmPage.class;
        }

        @Override
        public FakeIcmPage create(final WebDriver driver) {
            return new FakeIcmPage(driver);
        }
    }
}
