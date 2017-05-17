/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindService;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.LoginService;

public abstract class FindApplication<T extends FindElementFactory> implements Application<T> {
    private LoginService loginService;

    public static FindApplication<?> ofType(final ApplicationType type) {
        switch(type) {
            case HOSTED:
                return HodFind.withRole(UserRole.activeRole());
            case ON_PREM:
                return IdolFind.withRole(UserRole.activeRole());
            default:
                throw new IllegalStateException("Unsupported app type: " + type);
        }
    }

    @Override
    public LoginService loginService() {
        if(loginService == null) {
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
}
