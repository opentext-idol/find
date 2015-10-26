package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Yahoo implements AuthProvider {
    private final String email;
    private final String password;
    private WebDriver driver;

    public Yahoo(){
        this.email = System.getProperty("com.autonomy.username");
        this.password = System.getProperty("com.autonomy.password");
    }

    private WebElement yahooButton(){
        return driver.findElement(By.linkText("Yahoo"));
    }

    private WebElement usernameBox(){
        return driver.findElement(By.id("login-username"));
    }

    private WebElement passwordBox(){
        return driver.findElement(By.id("login-passwd"));
    }

    private WebElement signInBtn(){
        return driver.findElement(By.id("login-signin"));
    }

    @Override
    public void login(WebDriver driver) {
        this.driver = driver;
        yahooButton().click();
        usernameBox().sendKeys(email);
        passwordBox().sendKeys(password);
        signInBtn().click();
    }
}
