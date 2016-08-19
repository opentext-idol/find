package com.autonomy.abc.selenium.auth;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.WebDriver;

public final class UnknownAuth implements AuthProvider {
    private static final UnknownAuth INSTANCE = new UnknownAuth();

    private UnknownAuth() {}

    @Override
    public void login(final WebDriver driver) {
        throw new UnsupportedOperationException("cannot log in as this user");
    }

    public static AuthProvider getInstance() {
        return INSTANCE;
    }
}
