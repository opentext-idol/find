package com.autonomy.abc.base;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestUserRole extends TestWatcher {
    private Role userRole;

    @Override
    protected void starting(Description description) {
        userRole = description.getTestClass().getAnnotation(Role.class);

        Role methodAnnotation = description.getAnnotation(Role.class);
        if(methodAnnotation != null) {
            userRole = methodAnnotation;
        }
    }

    public UserRole getApplicationValue() {
        if (userRole == null) {
            return UserRole.ALL;
        }

        return userRole.value();
    }
}
