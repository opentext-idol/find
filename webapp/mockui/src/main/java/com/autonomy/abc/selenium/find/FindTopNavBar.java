package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindTopNavBar implements LoginService.LogoutHandler {
    private final WebElement header;
    private final FormInput input;

    public FindTopNavBar(final WebDriver driver) {
        header = driver.findElement(By.className("header"));
        input = new FormInput(driver.findElement(By.cssSelector(".input-view-container .find-input")), driver);
    }

    @Override
    public void logOut() {
        header.findElement(By.className("hp-settings")).click();
        header.findElement(By.className("navigation-logout")).click();
        Waits.loadOrFadeWait();
    }

    public String getSearchBoxTerm() {
        return input.getValue();
    }

    public FormInput getSearchBoxInput() {
        return input;
    }

    public String getCurrentUsername() {
        return header.findElement(By.className("navbar-username")).getText();
    }
}
