package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GoogleAuth implements AuthProvider {
    private String email;
    private String password;

    public GoogleAuth(String email, String password) {
        this.email = "skdghfd.dfshgksdgh@gmail.com";
        this.password = "CX):35mFBftVE~j";
    }

    private WebElement getLoginButton(WebDriver driver) {
        return driver.findElement(By.linkText("Google"));
    }

    private WebElement getEmailInput(WebDriver driver) {
        return new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.id("Email")));
    }

    private WebElement getEmailSubmitButton(WebDriver driver) {
        return driver.findElement(By.id("next"));
    }

    private WebElement getPasswordInput(WebDriver driver) {
        return new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id("Passwd")));
    }

    private WebElement getPasswordSubmitButton(WebDriver driver) {
        return driver.findElement(By.id("signIn"));
    }

    @Override
    public void login(AppElement loginPage) {
        WebDriver driver = loginPage.getDriver();
        getLoginButton(driver).click();
        getEmailInput(driver).sendKeys(email);
        getEmailSubmitButton(driver).click();
        getPasswordInput(driver).sendKeys(password);
        getPasswordSubmitButton(driver).click();
    }
}
