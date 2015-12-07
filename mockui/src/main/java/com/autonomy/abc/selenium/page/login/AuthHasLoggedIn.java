package com.autonomy.abc.selenium.page.login;

import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.WebDriver;

public class AuthHasLoggedIn implements HasLoggedIn {
    private final WebDriver driver;

    public AuthHasLoggedIn(final WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean hasLoggedIn() {
        String url = driver.getCurrentUrl();
        return url.contains("sso") && !url.contains("verification");
    }
}
