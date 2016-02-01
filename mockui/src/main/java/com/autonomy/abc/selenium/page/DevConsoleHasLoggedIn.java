package com.autonomy.abc.selenium.page;

import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class DevConsoleHasLoggedIn implements HasLoggedIn {
    WebDriver driver;

    public DevConsoleHasLoggedIn (WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean hasLoggedIn() {
        try {
            driver.findElement(By.className("iod-logo-nav"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
