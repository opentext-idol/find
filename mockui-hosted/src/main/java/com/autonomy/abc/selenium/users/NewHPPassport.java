package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.sso.HPPassport;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

//TODO need a way to convert this to an actual HPPassport?
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
        try {
            newAccountButton().click();
            emailInput().setValue(email);
            passwordInput().setValue(password);
            confirmPasswordInput().setValue(password);
            createButton().click();

            WebElement error = new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(driver.findElement(By.className("message-error"))));
            if(error.getText().contains("email address is already registered")) {
                existingAccountButton().click();
                throw new UnsupportedOperationException();
            }
        } catch (ElementNotVisibleException | NoSuchElementException | UnsupportedOperationException e) {
            new HPPassport(email, password).login(driver);
        } catch (TimeoutException e) {
            //Expected if working
        }
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

    private WebElement existingAccountButton(){
        return driver.findElement(By.xpath("//*[text()='Existing account']"));
    }
}
