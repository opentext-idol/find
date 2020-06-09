/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
