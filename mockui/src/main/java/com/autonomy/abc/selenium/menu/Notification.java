package com.autonomy.abc.selenium.menu;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class Notification {
    private final String message;
    private final String username;
    private final String time;

    public Notification(String message, String username, String time){
        this.message = message;
        this.username = username;
        this.time = time;
    }

    public Notification(WebElement notification){
        this(
                notification.findElement(By.className("notification-message")).getText(),
                notification.findElement(By.cssSelector(".small:not(.notification-time)")).getText(),
                notification.findElement(By.className("notification-time")).getText()
        );
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getTime() {
        return time;
    }
}
