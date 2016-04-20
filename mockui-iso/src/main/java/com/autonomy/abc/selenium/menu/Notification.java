package com.autonomy.abc.selenium.menu;

import org.openqa.selenium.By;
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
                notification.findElement(By.className("pull-right")).getText()
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Notification){
            Notification notification = (Notification) obj;
            return notification.getMessage().equals(message) && notification.getUsername().equals(username);
        }

        return false;
    }

    @Override
    public String toString() {
        return getMessage() + " " + getTime() + ", performed by " + getUsername();
    }
}
