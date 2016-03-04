package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class NewHPPassport implements AuthProvider {

    private final String email;
    private final String password;

    private WebDriver driver;

    public NewHPPassport(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public void login(WebDriver driver) {
        this.driver = driver;
        newAccountButton().click();
        emailInput().setValue(email);
        passwordInput().setValue(password);
        confirmPasswordInput().setValue(password);
        createButton().click();
    }

    private WebElement createButton() {
        return driver.findElement(By.xpath("//*[text()='Create HP Passport and log in']"));
    }

    private WebElement newAccountButton() {
        return driver.findElement(By.xpath("//*[text()='New account']"));
    }

    private FormInput emailInput(){
        return new FormInput(driver.findElement(By.id("create-email")), driver);
    }

    private FormInput passwordInput(){
        return new FormInput(driver.findElement(By.id("create-password")), driver);
    }

    private FormInput confirmPasswordInput(){
        return new FormInput(driver.findElement(By.id("create-password-confirm")), driver);
    }
}
