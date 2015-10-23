package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Twitter implements AuthProvider {

    private final String username;
    private final String password;
    private WebDriver driver;

    public Twitter(String username, String password){
        this.username = username;
        this.password = password;
    }

    private WebElement usernameBox(){
        return driver.findElement(By.id("username_or_email"));
    }

    private WebElement passwordBox(){
        return driver.findElement(By.id("password"));
    }

    private WebElement submitBtn(){
        return driver.findElement(By.id("allow"));
    }

    @Override
    public void login(WebDriver driver) {
        this.driver = driver;
        driver.findElement(By.linkText("Twitter")).click();
        usernameBox().sendKeys(username);
        passwordBox().sendKeys(password);
        submitBtn().click();
    }
}
