package com.autonomy.abc.selenium.page.login;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OPLoginPage extends LoginPage {
    public OPLoginPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body"))), driver);
        waitForLoad();
    }

    @Override
    public void loginWith(AuthProvider authProvider) {
        authProvider.login(this);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("button")));
    }
}
