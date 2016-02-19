package com.autonomy.abc.config;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.find.HSODFindElementFactory;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.fail;

public abstract class FindTestBase extends SeleniumTest<HSODFind, HSODFindElementFactory> {
    private User initialUser;
    private User currentUser;

    protected FindTestBase(TestConfig config) {
        super(config, new HSODFind());
        this.initialUser = config.getDefaultUser();
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.HOSTED);
        return parameters(applicationTypes);
    }

    @Before
    public final void findSetUp() {
        if (!initialUser.equals(User.NULL)) {
            try {
                loginAs(initialUser);
                postLogin();
            } catch (Exception e) {
                LOGGER.error("Unable to login");
                LOGGER.error(e.toString());
                fail("Unable to login");
            }
        }
    }

    protected void postLogin() throws Exception {
    }

    protected final User getCurrentUser() {
        return currentUser;
    }

    protected final void loginAs(User user) {
        getElementFactory().getLoginPage().loginWith(user.getAuthProvider());
        currentUser = user;
    }
}
