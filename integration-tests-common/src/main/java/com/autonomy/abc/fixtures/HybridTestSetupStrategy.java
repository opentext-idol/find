package com.autonomy.abc.fixtures;

import com.autonomy.abc.selenium.actions.Command;
import com.autonomy.abc.selenium.actions.NullCommand;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HybridTestSetupStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(HybridTestSetupStrategy.class);
    private final Application<?> app;
    private final User initialUser;
    private boolean hasSetUp = false;

    public HybridTestSetupStrategy(Application<?> app, User initialUser) {
        this.app = app;
        this.initialUser = initialUser;
    }

    public void setUp(Command postSetUpHook) {
        if (!initialUser.equals(User.NULL)) {
            try {
                app.loginService().login(initialUser);
                postSetUpHook.execute();
            } catch (Exception e) {
                LOGGER.error("Unable to login");
                LOGGER.error(e.toString());
                throw new AssertionError("Unable to login", e);
            }
        }
        hasSetUp = true;
    }

    public void setUp() {
		setUp(NullCommand.getInstance());
    }

    public boolean hasSetUp() {
        return hasSetUp;
    }

    public User getInitialUser() {
        return initialUser;
    }
}
