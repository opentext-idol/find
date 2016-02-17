package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.WebDriver;

class NullUser extends User {
    private final static NullUser INSTANCE = new NullUser();

    private NullUser() {
        super(NullAuth.getInstance(), "", Role.NONE);
    }

    static NullUser getInstance() {
        return INSTANCE;
    }

    private static class NullAuth implements AuthProvider {
        private final static NullAuth INSTANCE = new NullAuth();

        @Override
        public void login(WebDriver webDriver) {
            /* NOOP */
        }

        private static NullAuth getInstance() {
            return INSTANCE;
        }
    }
}
