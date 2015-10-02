package com.autonomy.abc.selenium.menu.HSO;

import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSOTopNavBar extends TopNavBar {

    public HSOTopNavBar(WebDriver driver) {
        super(driver);
    }

    @Override
    public NotificationsDropDown getNotifications() {
        return new NotificationsDropDown(getDriver());
    }

    public void notificationsDropdown() {
        //I think the notification dropdown changes with every new notification, so need to use getDriver()
//        javascriptClick(getDriver().findElement(By.cssSelector("nav:not(.affix-clone) .count-info")));
        getDriver().findElement(By.cssSelector("nav:not(.affix-clone) .count-info")).click();
//        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("nav:not(.affix-clone) .notification-list")));
    }
}
