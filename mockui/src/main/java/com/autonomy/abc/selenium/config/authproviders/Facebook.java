package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Facebook implements AuthProvider {
    private final String email;
    private final String password;
    private WebDriver driver;

    public Facebook(){
        this.email = System.getProperty("com.autonomy.username");
        this.password = System.getProperty("com.autonomy.password");
    }

    private WebElement facebookButton(){
        return driver.findElement(By.linkText("Facebook"));
    }

    private WebElement usernameBox(){
        return driver.findElement(By.id("email"));
    }

    private WebElement passwordBox(){
        return driver.findElement(By.id("pass"));
    }

    private WebElement logInBtn(){
        return driver.findElement(By.id("u_0_2"));
    }

    @Override
    public void login(WebDriver driver) {
        this.driver = driver;
        facebookButton().click();
        usernameBox().sendKeys(email);
        passwordBox().sendKeys(password);
        logInBtn().click();
    }
}
