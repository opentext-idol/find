/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
