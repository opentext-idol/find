package com.autonomy.abc.users;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.users.IdolIsoUserService;
import com.autonomy.abc.shared.UserTestHelper;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasAttribute;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;

public class IdolUserPermissionsITCase extends IdolIsoTestBase {
    private final NewUser aNewUser;
    private UserTestHelper helper;

    public IdolUserPermissionsITCase(TestConfig config) {
        super(config);
        aNewUser = config.getNewUser("james");
    }

    @Before
    public void setUp() {
        helper = new UserTestHelper(getApplication(), getConfig());
        IdolIsoUserService userService = getApplication().userService();
        userService.deleteOtherUsers();
        new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationsDisappear());

        helper.signUpAndLoginAs(aNewUser, getWindow());
    }

    @After
    public void userTearDown() {
        if (hasSetUp()) {
            IsoApplication<?> tempApp = IsoApplication.ofType(getConfig().getType());
            Session tempSession = launchInNewSession(tempApp);
            tempApp.loginService().login(getInitialUser());
            tempApp.userService().deleteOtherUsers();
            getSessionRegistry().endSession(tempSession);
        }
    }

    @Test
    public void testAnyUserCanNotAccessConfigPage() {
        String baseUrl = getAppUrl();
        baseUrl = baseUrl.replace("/p/promotions","/config");
        getDriver().get(baseUrl);
        Waits.loadOrFadeWait();
        assertThat("Users are not allowed to access the config page", getDriver().findElement(By.tagName("body")), containsText("Authentication Failed"));
    }

    @Test
    public void testUserCannotAccessUsersPageOrSettingsPage() {
        getDriver().get(getAppUrl() + "settings");
        Waits.loadOrFadeWait();
        assertThat(getWindow(), url(not(containsString("settings"))));
        assertThat(getWindow(), urlContains("overview"));

        getDriver().get(getAppUrl() + "users");
        Waits.loadOrFadeWait();
        assertThat(getWindow(), url(not(containsString("users"))));
        assertThat(getWindow(), urlContains("overview"));
    }

    @Test
    public void testXmlHttpRequestToUserConfigBlockedForInadequatePermissions() throws UnhandledAlertException {
        final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("$.get('/api/admin/config/users').error(function(xhr) {$('body').attr('data-status-user', xhr.status);});");
        Waits.loadOrFadeWait();
        assertThat(getDriver().findElement(By.cssSelector("body")), hasAttribute("data-status-user", containsString("403")));

        this.helper.logoutAndNavigateToWebApp(getWindow());
        getApplication().loginService().login(getConfig().getDefaultUser());
        Waits.loadOrFadeWait();
        assertThat(getWindow(), url(not(containsString("login"))));

        executor.executeScript("$.get('/api/admin/config/users').error(function() {$('body').attr('data-status-admin', xhr.status);});");
        Waits.loadOrFadeWait();
        assertThat(getDriver().findElement(By.cssSelector("body")), not(hasAttribute("data-status-admin")));
    }
}
