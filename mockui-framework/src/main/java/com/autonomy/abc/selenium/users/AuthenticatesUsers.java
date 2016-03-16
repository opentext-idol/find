package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

public class AuthenticatesUsers {
    private final Factory<WebDriver> factory;
    private final GoesToAuthPage strategy;

    public AuthenticatesUsers(Factory<WebDriver> driverFactory, GoesToAuthPage authStrategy) {
        factory = driverFactory;
        strategy = authStrategy;
    }

    public void authenticate(User user) {
        user.authenticate(factory, strategy);
    }
}
