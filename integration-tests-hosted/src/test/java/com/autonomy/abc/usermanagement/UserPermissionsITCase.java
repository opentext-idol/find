package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.external.GmailSignupEmailHandler;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.util.PageUtil;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

public class UserPermissionsITCase extends HostedTestBase {
    public UserPermissionsITCase(TestConfig config) {
        super(config);
    }

    private UserService userService;
    private User user;

    private Session devSession;
    private Session userSession;

    private HSODApplication userApp;

    @Before
    public void setUp(){
        userService = getApplication().userService();
        user = userService.createNewUser(getConfig().generateNewUser(), Role.ADMIN);

        devSession = getMainSession();
        userApp = new HSODApplication();
        userSession = launchInNewSession(userApp);

        user.authenticate(getConfig().getWebDriverFactory(), new GmailSignupEmailHandler((GoogleAuth) user.getAuthProvider()));

        try {
            userApp.loginService().login(user);
        } catch (NoSuchElementException e) {
            assertThat(userSession.getDriver().getPageSource(), not(containsString("Authentication Failed")));
        }
    }

    @Test
    public void testCannotNavigate(){
        userService.deleteUser(user);
        userApp.switchTo(PromotionsPage.class);
        verifyThat(userSession.getDriver().findElement(By.partialLinkText("Google")), displayed());
    }
}
