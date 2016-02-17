package com.autonomy.abc.selenium.page;

import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ErrorPage implements AppPage {
    private WebDriver driver;

    public ErrorPage(WebDriver driver) {
        this.driver = driver;
        waitForLoad();
    }

    public String getErrorCode() {
        return driver.findElement(By.tagName("h1")).getText();
    }

    public String getErrorTitle() {
        return driver.findElement(By.tagName("h3")).getText();
    }

    public String getErrorDescription() {
        return driver.findElement(By.cssSelector(".error-desc p")).getText();
    }

    public WebElement redirectButton() {
        return driver.findElement(By.tagName("a"));
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(driver, 20)
                .withMessage("loading error page")
                .until(ExpectedConditions.presenceOfElementLocated(By.className("error-desc")));
    }
}
