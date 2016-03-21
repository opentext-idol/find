package com.autonomy.abc.selenium.users;

import org.openqa.selenium.WebDriver;

public final class NullAuthenticationStrategy implements AuthenticationStrategy {
    private static NullAuthenticationStrategy INSTANCE = new NullAuthenticationStrategy();

    private NullAuthenticationStrategy() {}

    public static AuthenticationStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public void authenticate(User user) {}

    @Override
    public void cleanUp(WebDriver driver) {}
}
