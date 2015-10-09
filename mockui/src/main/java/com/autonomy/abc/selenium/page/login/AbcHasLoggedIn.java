package com.autonomy.abc.selenium.page.login;

import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AbcHasLoggedIn implements HasLoggedIn {

    private final WebDriver driver;

    public AbcHasLoggedIn(final WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean hasLoggedIn() {
        try {
            new WebDriverWait(driver, 60).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("navbar-static-top")));
        } catch (final TimeoutException e) {
            return false;
        }
        return true;
    }
}
