/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription.Acceptable;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.runner.Description;

import java.util.Optional;

public class UserRoleStrategy implements Acceptable {
    private final UserRole activeRole;

    UserRoleStrategy(final UserRole activeRole) {
        this.activeRole = activeRole;
    }

    @Override
    public Matcher<? super Description> asMatcher() {
        return new TypeSafeMatcher<Description>() {
            @Override
            protected boolean matchesSafely(final Description description) {
                final Optional<Role> maybeMethodAnnotation = Optional.ofNullable(description.getAnnotation(Role.class));

                final Optional<Role> maybeAnnotation = maybeMethodAnnotation.isPresent()
                        ? maybeMethodAnnotation
                        : Optional.ofNullable(description.getTestClass().getAnnotation(Role.class));

                if (maybeAnnotation.isPresent()) {
                    final UserRole requiredRole = maybeAnnotation.get().value();
                    return requiredRole == activeRole;
                } else {
                    return true;
                }
            }

            @Override
            public void describeTo(final org.hamcrest.Description description) {
                description.appendText("match between current user role and test");
            }
        };
    }
}
