package com.autonomy.abc.selenium.page.login;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// TODO: move this into hod-sso-page package
public class GoogleAuth implements AuthProvider {
    private final String email;
    private final String password;

    public GoogleAuth(final String email, final String password) {
        this.email = email;
        this.password = password;
    }

    private WebElement getLoginButton(final WebDriver driver) {
        return driver.findElement(By.linkText("Google"));
    }

    @Override
    public void login(final WebDriver driver) {
        getLoginButton(driver).click();
        new GoogleLoginPage(driver).login(this);
    }

    @Override
    public String toString() {
        return "Google:" + email;
    }

    public static class GoogleLoginPage {
        private WebDriver driver;

        public GoogleLoginPage(WebDriver driver) {
            this.driver = driver;
        }

        private WebElement emailInput() {
            return new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.id("Email")));
        }

        private WebElement passwordInput() {
            return new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id("Passwd")));
        }

        public void login(GoogleAuth auth) {
            emailInput().sendKeys(auth.email);
            emailInput().submit();
            passwordInput().sendKeys(auth.password);
            passwordInput().submit();
        }
    }
}
