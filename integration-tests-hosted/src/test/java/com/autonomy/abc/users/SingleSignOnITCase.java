package com.autonomy.abc.users;

import com.autonomy.abc.base.IsoHsodTestBase;
import com.autonomy.abc.selenium.devconsole.DevConsole;
import com.autonomy.abc.selenium.devconsole.DevConsoleHomePage;
import com.autonomy.abc.selenium.devconsole.HSODLandingPage;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.HsodFind;
import com.autonomy.abc.selenium.hsod.IsoHsodApplication;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.openqa.selenium.lift.Matchers.displayed;

/*
 * TODO Possibly make sure a gritter with 'Signed in' comes up, correct colour circle etc. May be difficult to do considering it occurs during tryLogIn()
 */
// these tests check that logging in/out of one app also logs in/out of another
public class SingleSignOnITCase extends IsoHsodTestBase {
    private IsoHsodApplication searchApp;
    private HsodFind findApp;
    private DevConsole devConsole;

    public SingleSignOnITCase(TestConfig config) {
        super(config);
        setInitialUser(User.NULL);
    }

    @Before
    public void setUp() {
        searchApp = getApplication();
        findApp = new HsodFind(getWindow());
        devConsole = new DevConsole(getWindow());

        // wait before doing anything
        getElementFactory().getLoginPage();
    }

    @Test
    public void testLogInSearchOptimizerToFind(){
        verifyLogin(searchApp, PromotionsPage.class);
        verifyRedirect(findApp, FindPage.class);
    }

    @Test
    public void testLoginFindToSearchOptimizer(){
        verifyLogin(findApp, FindPage.class);
        verifyRedirect(searchApp, PromotionsPage.class);
    }

    @Test
    public void testLogOutSearchOptimizerToFind(){
        loginLogout(searchApp, PromotionsPage.class);
        verifyRedirect(findApp, LoginPage.class);
    }

    @Test
    @RelatedTo("CSA-1674")
    public void testLogOutSearchOptimizerRedirect() {
        loginLogout(searchApp, PromotionsPage.class);
        verifyOn(devConsole, HSODLandingPage.class);
        verifyThat(devConsoleLoginButton(), displayed());
    }

    @Test
    @KnownBug("CSA-1854")
    public void testLogOutFindToSearchOptimizer(){
        loginLogout(findApp, FindPage.class);
        verifyOn(findApp, LoginPage.class);
        verifyThat(getWindow(), urlContains("find"));

        verifyRedirect(searchApp, LoginPage.class);
        verifyThat(getWindow(), urlContains("search"));

    }

    @Test
    //Assume that logging into Search/Find are the same
    public void testLoginSSOtoDevConsole(){
        verifyLogin(searchApp, PromotionsPage.class);
        verifyRedirect(devConsole, DevConsoleHomePage.class);
        verifyThat(devConsoleLoginButton(), not(displayed()));
    }

    @Test
    public void testLoginDevConsoletoSSO() {
        verifyLogin(devConsole, DevConsoleHomePage.class);
        verifyRedirect(searchApp, PromotionsPage.class);
    }

    @Test
    public void testLogoutSSOtoDevConsole() {
        loginLogout(searchApp, PromotionsPage.class);
        verifyRedirect(devConsole, DevConsoleHomePage.class);
        verifyThat(devConsoleLoginButton(), displayed());
    }

    @Test
    public void testLogoutDevConsoletoSSO() {
        loginLogout(devConsole, DevConsoleHomePage.class);
        verifyRedirect(searchApp, LoginPage.class);
    }

    private <T extends AppPage> void verifyLogin(Application<?> app, Class<T> pageType) {
        redirectTo(app);
        app.loginService().login(getConfig().getDefaultUser());
        verifyOn(app, pageType);
    }

    private <T extends AppPage> void loginLogout(Application<?> app, Class<T> pageType) {
        verifyLogin(app, pageType);
        app.loginService().logout();
    }

    private <T extends AppPage> void verifyRedirect(Application<?> app, Class<T> pageType) {
        redirectTo(app);
        verifyOn(app, pageType);
    }

    private <T extends AppPage> void verifyOn(Application<?> app, Class<T> pageType) {
        T page = null;
        try {
            page = app.elementFactory().loadPage(pageType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verifyThat("on " + pageType.getSimpleName(), page, not(nullValue()));
    }

    private WebElement devConsoleLoginButton() {
        return devConsole.elementFactory().getTopNavBar().loginButton();
    }
}
