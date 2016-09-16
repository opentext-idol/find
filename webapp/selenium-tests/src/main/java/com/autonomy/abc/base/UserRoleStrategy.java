package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.runner.Description;

public class UserRoleStrategy implements RunOnlyIfDescription.Acceptable {

    private final UserRole configUserRole;

    public UserRoleStrategy() {
        String userRole = System.getProperty("userRole");

        configUserRole = userRole == null ? null : UserRole.fromString(userRole);
    }

    @Override
    public Matcher<? super Description> asMatcher() {

        return new TypeSafeMatcher<Description>() {
            @Override
            protected boolean matchesSafely(Description item) {
                TestUserRole userRole = new TestUserRole();
                userRole.starting(item);

                //Test not annotated
                if(userRole.isNull()){
                    return true;
                }
                UserRole testUserRole = userRole.getUserRoleValue();

                return (configUserRole==null && testUserRole.equals(UserRole.BIFHI)) || (configUserRole!=null && configUserRole.equals(testUserRole));
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("Tests run against User Role specified");
            }
        };
    }
}
