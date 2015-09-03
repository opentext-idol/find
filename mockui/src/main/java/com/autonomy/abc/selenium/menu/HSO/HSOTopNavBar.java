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
        return null;
    }

    public void notificationsDropdown() {
        findElement(By.className("count-info")).click();
    }
}
