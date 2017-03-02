package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class DashboardPage extends AppElement {

    DashboardPage(final WebDriver driver) {
        super(new WebDriverWait(driver, 30)
                .withMessage("loading Dashboard page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("dashboard"))), driver);
    }

    public static class Factory implements ParametrizedFactory<WebDriver, DashboardPage> {
        @Override
        public DashboardPage create(final WebDriver context) {
            return new DashboardPage(context);
        }
    }

    public List<WebElement> getWidgets() {
        return getDriver().findElements(By.cssSelector(".widget"));
    }

}
