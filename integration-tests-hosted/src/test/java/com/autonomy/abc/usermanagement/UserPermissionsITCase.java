package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.external.GmailSignupEmailHandler;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.users.UserService;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("HOD-532")
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
        GoogleAuth googleAuth = (GoogleAuth) userService.createNewUser(getConfig().generateNewUser(), Role.ADMIN).getAuthProvider();

        user = userService.createNewUser(getConfig().getNewUser("newhppassport"), Role.ADMIN);

        devSession = getMainSession();
        userApp = new HSODApplication();
        userSession = launchInNewSession(userApp);

        user.authenticate(getConfig().getWebDriverFactory(), new GmailSignupEmailHandler(googleAuth));

        try {
            userApp.loginService().login(user);
        } catch (NoSuchElementException e) {
            assumeThat("Authentication failed", userSession.getDriver().getPageSource(), not(containsString("Authentication Failed")));
            assumeThat("Promotions page not displayed", userApp.elementFactory().getPromotionsPage(), displayed());
        }
    }

    @After
    public void tearDown(){
        userService.deleteOtherUsers();
    }

    @Test
    public void testCannotNavigate(){
        verifyThat(userApp.elementFactory().getPromotionsPage(), displayed());
        userService.deleteUser(user);

        try {
            userApp.switchTo(AnalyticsPage.class);
        } catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e){
            //Expected as you'll be logged out
        }

        new WebDriverWait(userSession.getDriver(), 10).until(ExpectedConditions.titleIs("Haven Search OnDemand - Error"));

        verifyThat(userSession.getDriver().getPageSource(), containsString("Authentication Failed"));
    }
}
