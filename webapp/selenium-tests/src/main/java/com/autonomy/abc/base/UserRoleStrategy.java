package com.autonomy.abc.base;

import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.runner.Description;

public class UserRoleStrategy implements RunOnlyIfDescription.Acceptable {

    private final UserRole configUserRole;

    public UserRoleStrategy() {
        this.configUserRole = UserRole.fromString(System.getProperty("userRole"));
    }

    @Override
    public Matcher<? super Description> asMatcher() {

        return new TypeSafeMatcher<Description>() {
            @Override
            protected boolean matchesSafely(Description item) {
                TestUserRole userRole = new TestUserRole();
                userRole.starting(item);
                UserRole testUserRole = userRole.getApplicationValue();

                return configUserRole == null || configUserRole.equals(UserRole.ALL) || configUserRole.equals(testUserRole);
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("User Role correct within Config");
            }
        };
    }
}
