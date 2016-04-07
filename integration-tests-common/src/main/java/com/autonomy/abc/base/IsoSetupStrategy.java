package com.autonomy.abc.base;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.hp.autonomy.frontend.selenium.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsoSetupStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsoSetupStrategy.class);
    private final SearchOptimizerApplication<?> app;
    private final User initialUser;
    private boolean hasSetUp = false;

    public IsoSetupStrategy(SearchOptimizerApplication<?> app, User initialUser) {
        this.app = app;
        this.initialUser = initialUser;
    }

    protected void postSetUp() throws Exception {
        //Wait for page to load
        Thread.sleep(2000);
        // wait for the first page to load
        app.elementFactory().getPromotionsPage();
    }

    public void setUp() {
		if (!initialUser.equals(User.NULL)) {
			try {
				app.loginService().login(initialUser);
				postSetUp();
			} catch (Exception e) {
                LOGGER.error("Unable to login");
                LOGGER.error(e.toString());
                throw new AssertionError("Unable to login", e);
            }
        }
        hasSetUp = true;
    }

    public boolean hasSetUp() {
        return hasSetUp;
    }

    public User getInitialUser() {
        return initialUser;
    }
}
