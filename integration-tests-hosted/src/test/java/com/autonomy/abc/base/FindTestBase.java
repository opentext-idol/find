package com.autonomy.abc.base;

import com.autonomy.abc.config.SOConfigLocator;
import com.hp.autonomy.frontend.selenium.base.SeleniumTest;
import com.hp.autonomy.frontend.selenium.base.TestParameterFactory;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.find.HSODFindElementFactory;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public abstract class FindTestBase extends SeleniumTest<HSODFind, HSODFindElementFactory> {
    private User initialUser;
    private User currentUser;

    protected FindTestBase(TestConfig config) {
        super(config, new HSODFind());
        this.initialUser = config.getDefaultUser();
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return new TestParameterFactory().create(new SOConfigLocator().getJsonConfig());
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
