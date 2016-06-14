package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindService;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.LoginService;

public abstract class FindApplication<T extends FindElementFactory> implements Application<T> {
    private LoginService loginService;

    @Override
    public LoginService loginService() {
        if (loginService == null) {
            loginService = new LoginService(this);
        }
        return loginService;
    }

    public FindService findService() {
        return new FindService(this);
    }

    @Override
    public String getName() {
        return "Find";
    }

    public static FindApplication<?> ofType(final ApplicationType type) {
        switch (type) {
            case HOSTED:
                return new HsodFind();
            case ON_PREM:
                return new IdolFind();
            default:
                throw new IllegalStateException("Unsupported app type: " + type);
        }
    }
}
