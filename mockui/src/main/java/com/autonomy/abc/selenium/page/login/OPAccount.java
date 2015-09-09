package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class OPAccount implements AuthProvider {
    private String username;
    private String password;

    public OPAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void login(AppElement loginPage) {
        WebElement usernameField = loginPage.getDriver().findElement(By.cssSelector("[name='username']"));
        usernameField.clear();
        usernameField.sendKeys(username);
        WebElement passwordField = loginPage.getDriver().findElement(By.cssSelector("[name='password']"));
        passwordField.clear();
        passwordField.sendKeys(password);
        passwordField.submit();
    }
}
