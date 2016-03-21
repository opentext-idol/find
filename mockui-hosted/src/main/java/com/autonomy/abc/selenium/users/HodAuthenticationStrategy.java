package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Factory;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

public class HodAuthenticationStrategy implements AuthenticationStrategy {
    private final Factory<WebDriver> factory;
    private final GoesToAuthPage strategy;

    public HodAuthenticationStrategy(Factory<WebDriver> driverFactory, GoesToAuthPage authStrategy) {
        factory = driverFactory;
        strategy = authStrategy;
    }

    @Override
    public void authenticate(User user) {
        WebDriver driver = factory.create();

        try {
            strategy.tryGoingToAuthPage(driver);
            LoginPage loginPage = new HSOLoginPage(driver, new AuthHasLoggedIn(driver));
            try {
                loginPage.loginWith(user.getAuthProvider());
            } catch (TimeoutException e) {
            /* already signed in to auth account */
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    @Override
    public void cleanUp(WebDriver driver) {
        strategy.cleanUp(driver);
    }
}
