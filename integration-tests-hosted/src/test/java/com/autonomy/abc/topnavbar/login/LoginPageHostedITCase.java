package com.autonomy.abc.topnavbar.login;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.devconsole.DevConsole;
import com.autonomy.abc.selenium.devconsole.DevConsoleElementFactory;
import com.autonomy.abc.selenium.devconsole.HSODLandingPage;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.find.HSODFindElementFactory;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

/*
 * TODO Possibly make sure a gritter with 'Signed in' comes up, correct colour circle etc. May be difficult to do considering it occurs during tryLogIn()
 */
public class LoginPageHostedITCase extends HostedTestBase {
    private HSODFind findApp;
    private HSODFindElementFactory findFactory;
    private DevConsole devConsole;
    private DevConsoleElementFactory devFactory;

    public LoginPageHostedITCase(TestConfig config) {
        super(config);
        setInitialUser(User.NULL);
    }

    @Before
    public void setUp() {
        findApp = new HSODFind(getMainSession().getActiveWindow());
        findFactory = findApp.elementFactory();
        devConsole = new DevConsole(getMainSession().getActiveWindow());
        devFactory = devConsole.elementFactory();
    }

    @Test   @Ignore("No account")
    public void testAPIKeyLogin(){
       testLogin("api_key");
    }

    @Test
    public void testGoogleLogin(){
        testLogin("google");
    }

    @Test
    public void testTwitterLogin(){
        testLogin("twitter");
    }

    @Test
    public void testFacebookLogin(){
        testLogin("facebook");
    }

    @Test
    public void testYahooLogin(){
        testLogin("yahoo");
    }

    @Test   @Ignore("No account")
    public void testOpenIDLogin(){
        testLogin("open_id");
    }

    @Test
    public void testHPPassportLogin(){
        testLogin("hp_passport");
    }

    private void testLogin(String account) {
        try {
            loginAs(getConfig().getUser(account));
        } catch (Exception e) {
            throw new AssertionError("unable to log in as " + account, e);
        }
    }

    // these tests check that logging in/out of one app also logs in/out of another
    @Test
    public void testLogInSearchOptimizerToFind(){
        User user = getConfig().getDefaultUser();
        loginAs(user);

        getDriver().navigate().to(getConfig().getAppUrl(findApp));
        verifyThat(findFactory.getFindPage(), displayed());
    }

    @Test
    public void testLoginFindToSearchOptimizer(){
        loginThroughFind();

        getDriver().navigate().to(getAppUrl());
        verifyThat(getElementFactory().getPromotionsPage(), displayed());
    }

    @Test
    public void testLogOutSearchOptimizerToFind(){
        loginAs(getConfig().getDefaultUser());

        logout();

        getDriver().navigate().to(getConfig().getAppUrl(findApp));
        verifyOnSSOPage();
    }

    @Test
    @RelatedTo("CSA-1674")
    public void testLogOutSearchOptimizerRedirect() {
        loginAs(getConfig().getDefaultUser());
        logout();

        HSODLandingPage page = null;
        try {
            page = devFactory.getHSODPage();
        } catch (Exception e) {
            /* noop */
        }

        verifyThat(page, not(nullValue()));
        verifyThat(devFactory.getTopNavBar().loginButton(), displayed());
    }

    @Test
    @KnownBug("CSA-1854")
    public void testLogOutFindToSearchOptimizer(){
        loginThroughFind();

        FindPage findPage = findFactory.getFindPage();
        findPage.logOut();

        verifyOnSSOPage();
        verifyThat(getDriver().getCurrentUrl(), containsString("find"));

        getDriver().navigate().to(getAppUrl());
        verifyOnSSOPage();
        verifyThat(getDriver().getCurrentUrl(), containsString("search"));

    }

    @Test
    //Assume that logging into Search/Find are the same
    public void testLoginSSOtoDevConsole(){
        loginAs(getConfig().getDefaultUser());

        getDriver().navigate().to(getConfig().getAppUrl(devConsole));

        verifyThat(devFactory.getHomePage(), displayed());
        verifyThat(devFactory.getTopNavBar().loginButton(), not(displayed()));
    }

    @Test
    public void testLoginDevConsoletoSSO() {
        loginThroughDevConsole();

        getDriver().navigate().to(getAppUrl());
        verifyThat(getElementFactory().getPromotionsPage(), displayed());
    }

    @Test
    public void testLogoutSSOtoDevConsole() {
        loginAs(getConfig().getDefaultUser());

        logout();

        getDriver().navigate().to(getConfig().getAppUrl(devConsole));
        verifyThat(devFactory.getHomePage(), displayed());
        verifyThat(devFactory.getTopNavBar().loginButton(), displayed());
    }

    @Test
    public void testLogoutDevConsoletoSSO() {
        loginThroughDevConsole();
        devFactory.getTopNavBar().logOut();

        getDriver().navigate().to(getAppUrl());
        verifyOnSSOPage();
    }

    private void loginThroughDevConsole() {
        getDriver().navigate().to(getConfig().getAppUrl(devConsole));
        devFactory.getTopNavBar().loginButton().click();
        loginTo(devFactory.getLoginPage(), getDriver(), getConfig().getDefaultUser());
        devFactory.getHomePage();
    }

    private void loginThroughFind() {
        getElementFactory().getLoginPage();

        getDriver().navigate().to(getConfig().getAppUrl(findApp));
        loginTo(findFactory.getLoginPage(), getDriver(), getConfig().getDefaultUser());
    }

    private void verifyOnSSOPage() {
        LoginPage loginPage = null;
        try {
            loginPage = getElementFactory().getLoginPage();
        } catch (Exception e) {
            // NOOP
        }
        verifyThat("taken to SSO page", loginPage, not(nullValue()));
    }
}
