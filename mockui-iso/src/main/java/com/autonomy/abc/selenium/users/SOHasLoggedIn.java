package com.autonomy.abc.selenium.users;

import com.autonomy.abc.config.DualConfigLocator;
import com.autonomy.abc.selenium.login.SSOFailureException;
import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOHasLoggedIn implements HasLoggedIn {
    private final WebDriver driver;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOHasLoggedIn.class);

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
        getCorrectURL(driver);
        return true;
    }

    private static void getCorrectURL(final WebDriver input) {
        String correctURL;
        try {
            correctURL = new DualConfigLocator()
                    .getJsonConfig()
                    .getAppUrl("search")
                    .toString();
        } catch (Exception e) {
            LOGGER.warn("Could not get correct URL from config file to redirect to it");
            correctURL = input.getCurrentUrl();
        }
        if (!input.getCurrentUrl().equals(correctURL)) {
                input.navigate().to(correctURL);
        }
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
            return !driver.findElements(By.className("navbar")).isEmpty();
        }

        private boolean signedInTextVisible(final WebDriver driver) {
            return !driver.findElements(By.xpath("//*[text()='Signed in']")).isEmpty();
        }
    }
}
