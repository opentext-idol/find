package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolIsoAccount implements AuthProvider {
    private final String username;
    private final String password;

    public IdolIsoAccount(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void login(final WebDriver driver) {
        final WebElement usernameField = driver.findElement(By.cssSelector("[name='username']"));
        usernameField.clear();
        usernameField.sendKeys(username);

        final WebElement passwordField = driver.findElement(By.cssSelector("[name='password']"));
        passwordField.clear();
        passwordField.sendKeys(password);
        passwordField.submit();
    }

    @Override
    public String toString() {
        return "IdolIsoAccount:" + username;
    }
}
