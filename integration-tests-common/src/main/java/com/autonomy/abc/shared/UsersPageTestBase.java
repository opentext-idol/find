package com.autonomy.abc.shared;

import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.base.SOTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.After;
import org.junit.Before;

import java.net.MalformedURLException;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.not;

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

    protected User singleSignUp() {
        return helper.singleSignUp(aNewUser);
    }

    protected void signUpAndLoginAs(T newUser) {
        helper.signUpAndLoginAs(newUser, getWindow());
    }

    protected void deleteAndVerify(User user) {
        helper.deleteAndVerify(user);
    }

    protected void verifyUserAdded(ModalView newUserModal, User user){
        helper.verifyUserAdded(newUserModal, user);
    }

    protected void logoutAndNavigateToWebApp() {
        helper.logoutAndNavigateToWebApp(getWindow());
    }

    protected LoginService getLoginService() {
        return getApplication().loginService();
    }

    protected void verifyCreateDeleteInTable(NewUser newUser) {
        helper.verifyCreateDeleteInTable(newUser);
    }

    protected void loginAs(User user) {
        getLoginService().login(user);
    }

    protected void authenticate(User user) {
        getConfig().getAuthenticationStrategy().authenticate(user);
    }
}
