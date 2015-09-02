package com.autonomy.abc.selenium.menu;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class TopNavBar extends AppElement {
    public TopNavBar(WebDriver driver) {
        super(driver.findElement(By.cssSelector(".navbar-static-top")), driver);
    }

    public abstract NotificationsDropDown getNotifications();

}
