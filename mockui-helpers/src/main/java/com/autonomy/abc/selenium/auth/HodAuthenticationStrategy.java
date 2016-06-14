package com.autonomy.abc.selenium.auth;

import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Factory;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

public class HodAuthenticationStrategy implements AuthenticationStrategy {
    private final Factory<WebDriver> factory;
    private final GoesToAuthPage strategy;

    public HodAuthenticationStrategy(final Factory<WebDriver> driverFactory, final GoesToAuthPage authStrategy) {
        factory = driverFactory;
        strategy = authStrategy;
    }

    @Override
    public void authenticate(final User user) {
        final WebDriver driver = factory.create();

        try {
            strategy.tryGoingToAuthPage(driver);
            final LoginPage loginPage = new HSOLoginPage(driver, new AuthHasLoggedIn(driver));
            try {
                loginPage.loginWith(user.getAuthProvider());
            } catch (final TimeoutException e) {
            /* already signed in to auth account */
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    @Override
    public void cleanUp(final WebDriver driver) {
        strategy.cleanUp(driver);
    }
}
