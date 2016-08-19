package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class NotificationsDropDown extends AppElement {

    public NotificationsDropDown(final WebDriver driver) {
        super(driver.findElement(By.cssSelector("nav.affix-element .notification-list")), driver);
    }

    //TODO Even this may need to be abstracted - affix-element for Angular half
    //      Although it won't matter if the right Notifications Dropdown is given?
    public WebElement notificationNumber(final int index) {
        return findElement(By.cssSelector("li:nth-child(" + (index * 2 - 1) + ") a .notification-message"));
    }

    public List<Notification> getAllNotifications(){
        final List<Notification> notifications = new ArrayList<>();
        for(final WebElement notification : findElements(By.cssSelector("li:not(.no-notifications) a"))){
            if(notification.isDisplayed()) {
                notifications.add(new Notification(notification));
            }
        }
        return notifications;
    }

    public Notification getNotification(final int index){
        return new Notification(findElement(By.cssSelector("li:nth-child(" + (index * 2 - 1) + ") a")));
    }

    public List<String> getAllNotificationMessages(){
        final List<String> messages = new ArrayList<>();
        for(final WebElement notification : findElements(By.cssSelector("li a .notification-message"))){
            messages.add(notification.getText());
        }
        return messages;
    }

    public void toggleNotificationsOpen() {
        ElementUtil.getParent(this).click();
    }

    public int countNotifications() {
        return findElements(By.cssSelector(".notification-message")).size();
    }
}
