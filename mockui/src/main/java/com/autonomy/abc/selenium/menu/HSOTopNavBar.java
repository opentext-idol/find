package com.autonomy.abc.selenium.menu;

import org.openqa.selenium.WebDriver;

public class HSOTopNavBar extends TopNavBar {

    public HSOTopNavBar(WebDriver driver) {
        super(driver);
    }

    @Override
    public NotificationsDropDown getNotifications() {
        return null;
    }

}
