/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription.Acceptable;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.runner.Description;

public class UserRoleStrategy implements Acceptable {

    private final SeleniumTest<?, ?> test;
    private final UserRole configUserRole;

    public UserRoleStrategy(final SeleniumTest<?, ?> test) {
        this.test = test;
        final String userRole = System.getProperty("userRole");
        configUserRole = userRole == null ? null : UserRole.fromString(userRole);
    }

    @Override
    public Matcher<? super Description> asMatcher() {

        return new TypeSafeMatcher<Description>() {
            @Override
            protected boolean matchesSafely(final Description item) {
                final TestUserRole userRole = new TestUserRole();
                userRole.starting(item);

                final ApplicationType applicationType = test.getConfig().getType();

                //Test not annotated
                if(userRole.isNull()) {
                    return true;
                }
                final UserRole testUserRole = userRole.getUserRoleValue();

                return runAgainst(testUserRole, applicationType.equals(ApplicationType.HOSTED) ? UserRole.FIND : UserRole.BIFHI);

            }

            private boolean runAgainst(final UserRole testUserRole, final UserRole against) {
                return (configUserRole == null && testUserRole.equals(against)) ||
                        (configUserRole != null && configUserRole.equals(testUserRole)) ||
                        testUserRole.equals(UserRole.BOTH);
            }

            @Override
            public void describeTo(final org.hamcrest.Description description) {
                description.appendText("match between current user role and test");
            }
        };
    }
}
