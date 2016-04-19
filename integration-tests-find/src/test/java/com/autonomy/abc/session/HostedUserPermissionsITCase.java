package com.autonomy.abc.session;

import com.autonomy.abc.base.IsoHsodTestBase;
import com.autonomy.abc.fixtures.EmailTearDownStrategy;
import com.autonomy.abc.fixtures.UserTearDownStrategy;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.error.ErrorPage;
import com.autonomy.abc.selenium.find.HsodFind;
import com.autonomy.abc.selenium.users.HsodUserService;
import com.autonomy.abc.selenium.users.HsodUsersPage;
import com.autonomy.abc.selenium.users.Status;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.is;

// TODO: this was extracted from UserManagementHostedITCase and probably belongs in hsod-selenium
public class HostedUserPermissionsITCase extends IsoHsodTestBase {
    private HsodUserService userService;
    private HsodUsersPage usersPage;

    protected HostedUserPermissionsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        userService = getApplication().userService();
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
    }

    @After
    public void emailTearDown() {
        new EmailTearDownStrategy(getMainSession(), getConfig().getAuthenticationStrategy()).tearDown(this);
    }

    @After
    public void userTearDown() {
        new UserTearDownStrategy(getInitialUser()).tearDown(this);
    }

    @Test
    public void testNoneUserConfirmation() {
        NewUser somebody = getConfig().generateNewUser();
        User user = userService.createNewUser(somebody, Role.ADMIN);
        userService.changeRole(user, Role.NONE);
        verifyThat(usersPage.getStatusOf(user), is(Status.PENDING));

        getConfig().getAuthenticationStrategy().authenticate(user);
        waitForUserConfirmed(user);
        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));

        // TODO: use a single driver once 401 page has logout button
        IsoApplication<?> secondApp = IsoApplication.ofType(getConfig().getType());
        Window secondWindow = launchInNewSession(secondApp).getActiveWindow();
        try {
            try {
                secondApp.loginService().login(user);
            } catch (NoSuchElementException e) {
                /* Happens when it's trying to log in for the second time */
            }

            WebDriver secondDriver = secondWindow.getSession().getDriver();

            verify401(secondDriver);

            secondWindow.goTo(getAppUrl().split("/searchoptimizer")[0]);
            verify401(secondDriver);

            secondWindow.goTo(getConfig().getAppUrl(new HsodFind()));
            Waits.loadOrFadeWait();
            verifyThat(secondDriver.findElement(By.className("error-body")), containsText("401"));
        } finally {
            secondWindow.close();
        }
    }

    private void verify401(WebDriver driver){
        ErrorPage errorPage = new ErrorPage(driver);
        verifyThat(errorPage.getErrorCode(), is("401"));
    }


    @Test
    @KnownBug("HOD-532")
    public void testLogOutAndLogInWithNewUser() {
        final User user = userService.createNewUser(getConfig().generateNewUser(), Role.ADMIN);
        getConfig().getAuthenticationStrategy().authenticate(user);

        getApplication().loginService().logout();
        HsodFind findApp = new HsodFind();
        redirectTo(findApp);

        boolean success = true;
        try {
            findApp.loginService().login(user);
        } catch (Exception e) {
            success = false;
        }
        verifyThat("logged in", success);
        verifyThat("taken to Find", getDriver().getTitle(), containsString("Find"));
    }

    private void waitForUserConfirmed(User user){
        new WebDriverWait(getDriver(),30).pollingEvery(10, TimeUnit.SECONDS).withMessage("User not showing as confirmed").until(new WaitForUserToBeConfirmed(user));
    }

    private class WaitForUserToBeConfirmed implements ExpectedCondition<Boolean> {
        private final User user;

        WaitForUserToBeConfirmed(User user){
            this.user = user;
        }

        @Override
        public Boolean apply(WebDriver driver) {
            getWindow().refresh();
            usersPage = getElementFactory().getUsersPage();
            Waits.loadOrFadeWait();
            return usersPage.getStatusOf(user).equals(Status.CONFIRMED);
        }
    }
}
