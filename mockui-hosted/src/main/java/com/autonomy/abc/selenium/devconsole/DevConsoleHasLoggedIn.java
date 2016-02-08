package com.autonomy.abc.selenium.devconsole;

import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class DevConsoleHasLoggedIn implements HasLoggedIn {
    WebDriver driver;

    public DevConsoleHasLoggedIn (WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean hasLoggedIn() {
        try {
            new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(By.className("iod-logo-nav")));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
