package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class NotificationsDropDown extends AppElement {

    public NotificationsDropDown(WebDriver driver) {
        super(driver.findElement(By.cssSelector("nav:not(.affix-clone) .notification-list")), driver);
    }

    //TODO Even this may need to be abstracted - affix-element for Angular half
    //      Although it won't matter if the right Notifications Dropdown is given?
    public WebElement notificationNumber(final int index) {
        return findElement(By.cssSelector("li:nth-child(" + (index * 2 - 1) + ") a .notification-message"));
    }

    public List<String> getAllNotificationMessages(){
        List<String> messages = new ArrayList<>();
        for(WebElement notification : findElements(By.cssSelector("li a .notification-message"))){
            messages.add(notification.getText());
        }
        return messages;
    }

    public void toggleNotificationsOpen() {
        getParent(this).click();
    }

    public int countNotifications() {
        return findElements(By.cssSelector(".notification-message")).size();
    }
}
