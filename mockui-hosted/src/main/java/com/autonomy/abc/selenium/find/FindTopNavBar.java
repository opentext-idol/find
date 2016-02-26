package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.users.LoginService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class FindTopNavBar implements LoginService.LogoutHandler {
    private WebElement header;
    private FormInput input;

    FindTopNavBar(WebDriver driver) {
        this.header = driver.findElement(By.className("header"));
        this.input = new FormInput(driver.findElement(By.className("find-input")), driver);
    }

    @Override
    public void logOut() {
        findElement(By.className("hp-settings")).click();
        findElement(By.className("navigation-logout")).click();
    }

    public String getSearchBoxTerm() {
        return input.getValue();
    }

    public String getCurrentUsername() {
        return findElement(By.className("navbar-username")).getText();
    }

    private WebElement findElement(By locator) {
        return header.findElement(locator);
    }
}
