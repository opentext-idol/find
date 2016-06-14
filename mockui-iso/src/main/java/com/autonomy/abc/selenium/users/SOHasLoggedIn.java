package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.login.SSOFailureException;
import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SOHasLoggedIn implements HasLoggedIn {
    private final WebDriver driver;

    public SOHasLoggedIn(final WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean hasLoggedIn() {
        try {
            new WebDriverWait(driver, 60).until(new LoginCondition());
        } catch (final TimeoutException e) {
            return false;
        }
        return true;
    }

    private static class LoginCondition implements ExpectedCondition<Boolean> {
        @Override
        public Boolean apply(final WebDriver input) {
            if (navbarIsVisible(input)) {
                return true;
            } else if (signedInTextVisible(input)) {
                throw new SSOFailureException();
            } else {
                return false;
            }
        }

        private boolean navbarIsVisible(final WebDriver driver) {
            return driver.findElements(By.className("navbar-static-top")).size() > 0;
        }

        private boolean signedInTextVisible(final WebDriver driver) {
            return driver.findElements(By.xpath("//*[text()='Signed in']")).size() > 0;
        }
    }
}
