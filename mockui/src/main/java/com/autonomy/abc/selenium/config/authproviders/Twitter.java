package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Twitter implements AuthProvider {

    private final String username;
    private final String password;
    private WebDriver driver;

    public Twitter(){
        this.username = System.getProperty("com.autonomy.username");
        this.password = System.getProperty("com.autonomy.password");
    }

    private WebElement twitterButton(){
        return driver.findElement(By.linkText("Twitter"));
    }

    private WebElement usernameBox(){
        return driver.findElement(By.id("username_or_email"));
    }

    private WebElement passwordBox(){
        return driver.findElement(By.id("password"));
    }

    private WebElement signInBtn(){
        return driver.findElement(By.id("allow"));
    }

    @Override
    public void login(WebDriver driver) {
        this.driver = driver;
        twitterButton().click();
        usernameBox().sendKeys(username);
        passwordBox().sendKeys(password);
        signInBtn().click();
    }
}
