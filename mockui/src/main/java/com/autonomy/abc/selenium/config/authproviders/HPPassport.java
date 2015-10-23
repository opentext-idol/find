package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HPPassport implements AuthProvider {

    private final String username;
    private final String password;
    private WebDriver driver;

    public HPPassport(String username,String password){
        this.username = username;
        this.password = password;
    }

    private WebElement hpPassportButton(){
        return driver.findElement(By.linkText("Passport"));
    }

    private WebElement usernameBox(){
        return driver.findElement(By.id("hp-passport_username"));
    }

    private WebElement passwordBox(){
        return driver.findElement(By.id("hp-passport_password"));
    }

    private WebElement signInBtn(){
        return driver.findElement(By.id("hp-passport_submit"));
    }

    @Override
    public void login(WebDriver driver) {
        this.driver = driver;
        hpPassportButton().click();
        usernameBox().sendKeys(username);
        passwordBox().sendKeys(password);
        signInBtn().click();
    }
}
