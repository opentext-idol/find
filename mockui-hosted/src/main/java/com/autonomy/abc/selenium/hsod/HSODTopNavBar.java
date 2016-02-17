package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class HSODTopNavBar extends TopNavBar {

    HSODTopNavBar(WebDriver driver) {
        super(driver);
    }

    @Override
    public NotificationsDropDown getNotifications() {
        return new NotificationsDropDown(getDriver());
    }

    @Override
    public void logOut(){
        findElement(By.className("hp-settings")).click();
        Waits.loadOrFadeWait();

        findElement(By.className("navigation-logout")).click();
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("haven-splash-header")));
    }
}
