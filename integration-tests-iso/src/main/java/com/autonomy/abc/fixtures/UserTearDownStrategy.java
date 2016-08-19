package com.autonomy.abc.fixtures;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.users.User;

public class UserTearDownStrategy extends IsoTearDownStrategyBase {
    private final User survivor;

    public UserTearDownStrategy(final User survivor) {
        this.survivor = survivor;
    }

    @Override
    protected void cleanUpApp(final IsoApplication<?> app) {
        final LoginService service = app.loginService();
        if (service.getCurrentUser() == null) {
            service.login(survivor);
        } else if (!service.getCurrentUser().equals(survivor)) {
            service.logout();
            service.login(survivor);
        }
        app.userService().deleteOtherUsers();
    }
}
