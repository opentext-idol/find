package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;
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

    //Is this  misnamed?
    public UserRole getApplicationValue() {
        return userRole.value();
    }

    public boolean isNull() {
        return userRole == null;
    }
}
