package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.framework.environment.Deployment;
import com.hp.autonomy.frontend.selenium.framework.inclusion.RunOnlyIfDescription;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.runner.Description;

public class UserRoleStrategy implements RunOnlyIfDescription.Acceptable {

    private final SeleniumTest<?,?> test;
    private final UserRole configUserRole;

    public UserRoleStrategy(SeleniumTest<?, ?> test) {
        this.test = test;
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

                ApplicationType applicationType = test.getConfig().getType();

                //Test not annotated
                if(userRole.isNull()){
                    return true;
                }
                UserRole testUserRole = userRole.getUserRoleValue();

                return applicationType.equals(ApplicationType.HOSTED) ?
                        runAgainst(testUserRole, UserRole.FIND) : runAgainst(testUserRole, UserRole.BIFHI);

            }

            private boolean runAgainst(UserRole testUserRole, UserRole against) {
                return (configUserRole==null && testUserRole.equals(against)) ||
                        (configUserRole!=null && configUserRole.equals(testUserRole)) ||
                        testUserRole.equals(UserRole.BOTH);
            }

            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("match between current user role and test");
            }
        };
    }
}
