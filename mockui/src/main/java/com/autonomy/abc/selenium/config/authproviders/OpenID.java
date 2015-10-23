package com.autonomy.abc.selenium.config.authproviders;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OpenID implements AuthProvider {
    private final String url;
    private WebDriver driver;

    public OpenID(String url){
        this.url = url;
    }

    private WebElement openIDButton(){
        return driver.findElement(By.linkText("Open ID"));
    }

    private WebElement urlBox(){
        return driver.findElement(By.id("openid_identifier"));
    }

    private WebElement signInBtn(){
        return driver.findElement(By.id("openid_submit"));
    }

    @Override
    public void login(WebDriver driver) {
        this.driver = driver;
        openIDButton().click();
        urlBox().sendKeys(url);
        signInBtn().click();
    }
}
