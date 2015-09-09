package com.autonomy.abc.selenium.menu.HSO;

import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
        getDriver().findElement(By.className("count-info")).click();
    }
}
