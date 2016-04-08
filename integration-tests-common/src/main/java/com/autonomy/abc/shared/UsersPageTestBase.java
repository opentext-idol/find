package com.autonomy.abc.shared;

import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.base.SOTestBase;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.users.UsersPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import org.junit.After;
import org.junit.Before;

import java.net.MalformedURLException;

public class UsersPageTestBase<T extends NewUser> extends SOTestBase {
    protected final NewUser aNewUser = getConfig().getNewUser("james");
    protected final NewUser newUser2 = getConfig().getNewUser("john");
    protected int defaultNumberOfUsers = isHosted() ? 0 : 1;
    protected UsersPage usersPage;
    protected UserService<?> userService;
    private final UserTestHelper helper;

    public UsersPageTestBase(TestConfig config) {
        super(config);
        helper = new UserTestHelper(getApplication(), config);
    }

    @Before
    public void setUp() throws MalformedURLException, InterruptedException {
        userService = getApplication().userService();
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
    }

    @After
    public void emailTearDown() {
        if(hasSetUp() && isHosted()) {
            helper.deleteEmails(getMainSession());
        }
    }

    @After
    public void userTearDown() {
        SOTearDown.USERS.tearDown(this);
    }

}
