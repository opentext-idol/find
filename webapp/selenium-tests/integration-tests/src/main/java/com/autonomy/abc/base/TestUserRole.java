/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestUserRole extends TestWatcher {
    private Role userRole;

    @Override
    protected void starting(final Description description) {
        userRole = description.getTestClass().getAnnotation(Role.class);

        final Role methodAnnotation = description.getAnnotation(Role.class);
        if(methodAnnotation != null) {
            userRole = methodAnnotation;
        }
    }

    public UserRole getUserRoleValue() {
        return userRole.value();
    }

    public boolean isNull() {
        return userRole == null;
    }
}
