package com.autonomy.abc.selenium.menu.OP;

import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OPTopNavBar extends TopNavBar {
    public OPTopNavBar(WebDriver driver) {
        super(driver);
    }

    @Override
    public void logOut() {
        findElement(By.cssSelector(".dropdown-toggle .fa-cog")).click();
        new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOfElementLocated(By.className("navigation-logout"))).click();
    }
}
