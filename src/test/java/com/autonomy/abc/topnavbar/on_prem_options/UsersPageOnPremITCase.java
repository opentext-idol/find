package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnhandledAlertException;

import java.net.MalformedURLException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class UsersPageOnPremITCase extends UsersPageTestBase {

    private final NewUser aNewUser = config.getNewUser("james");
    private final NewUser newUser2 = config.getNewUser("john");
    private UsersPage usersPage;
    private UserService userService;

    public UsersPageOnPremITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp() throws MalformedURLException, InterruptedException {
        userService = getApplication().createUserService(getElementFactory());
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
    }

    @Test
    public void testAnyUserCanNotAccessConfigPage() {
        signUpAndLoginAs(aNewUser);

        String baseUrl = config.getWebappUrl();
        baseUrl = baseUrl.replace("/p/","/config");
        getDriver().get(baseUrl);
        usersPage.loadOrFadeWait();
        assertThat("Users are not allowed to access the config page", getDriver().findElement(By.tagName("body")), containsText("Authentication Failed"));
    }

    @Test
    public void testUserCannotAccessUsersPageOrSettingsPage() {
        signUpAndLoginAs(aNewUser);

        getDriver().get(config.getWebappUrl() + "settings");
        usersPage.loadOrFadeWait();
        assertThat(getDriver().getCurrentUrl(), not(containsString("settings")));
        assertThat(getDriver().getCurrentUrl(), containsString("overview"));

        getDriver().get(config.getWebappUrl() + "users");
        usersPage.loadOrFadeWait();
        assertThat(getDriver().getCurrentUrl(), not(containsString("users")));
        assertThat(getDriver().getCurrentUrl(), containsString("overview"));
    }

    @Test
    public void testChangeOfPasswordWorksOnLogin() {
        User initialUser = singleSignUp();
        User updatedUser = usersPage.changeAuth(initialUser, newUser2);

        logoutAndNavigateToWebApp();
        loginAs(initialUser);
        usersPage.loadOrFadeWait();
        assertThat("old password does not work", getDriver().getCurrentUrl(), containsString("login"));

        loginAs(updatedUser);
        usersPage.loadOrFadeWait();
        assertThat("new password works", getDriver().getCurrentUrl(), not(containsString("login")));
    }

    @Test
    public void testEditUserPassword() {
        assumeThat(config.getType(), is(ApplicationType.ON_PREM));
        User user = singleSignUp();

        Editable passwordBox = usersPage.passwordBoxFor(user);
        passwordBox.setValueAsync("");
        assertThat(passwordBox.getElement(), containsText("Password must not be blank"));
        assertThat(passwordBox.editButton(), not(displayed()));

        passwordBox.setValueAndWait("valid");
        assertThat(passwordBox.editButton(), displayed());
    }

    @Test
    public void testCreateUser() {
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
        verifyThat(newUserModal, hasTextThat(startsWith("Create New Users")));

        usersPage.createButton().click();
        verifyThat(newUserModal, containsText("Error! Username must not be blank"));

        usersPage.addUsername("Andrew");
        ((OPUsersPage) usersPage).clearPasswords();
        usersPage.createButton().click();
        verifyThat(newUserModal, containsText("Error! Password must not be blank"));

        usersPage.addAndConfirmPassword("password", "wordpass");
        usersPage.createButton().click();
        verifyThat(newUserModal, containsText("Error! Password confirmation failed"));

        usersPage.createNewUser("Andrew", "qwerty", "Admin");
        verifyThat(newUserModal, containsText("Done! User Andrew successfully created"));

        usersPage.closeModal();
        verifyThat(usersPage, not(containsText("Create New Users")));
    }

    @Test
    //TO BE MOVED BACK TO COMMON IF FUNCTIONALITY IS IMPLEMENTED
    public void testWontDeleteSelf() {
        assertThat(usersPage.deleteButton(getCurrentUser().getUsername()), disabled());
    }

    @Test
    public void testXmlHttpRequestToUserConfigBlockedForInadequatePermissions() throws UnhandledAlertException {
        signUpAndLoginAs(aNewUser);

        final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function(xhr) {$('body').attr('data-status', xhr.status);});");
        usersPage.loadOrFadeWait();
        Assert.assertTrue(getDriver().findElement(By.cssSelector("body")).getAttribute("data-status").contains("403"));

        logoutAndNavigateToWebApp();
        loginAs(config.getDefaultUser());
        usersPage.loadOrFadeWait();
        assertThat(getDriver().getCurrentUrl(), not(containsString("login")));

        executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function() {alert(\"error\");});");
        usersPage.loadOrFadeWait();
        assertThat(usersPage.isAlertPresent(), is(false));
    }

    @Test
    public void testAddStupidlyLongUsername() {
        final String longUsername = StringUtils.repeat("a", 100);

        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());

        usersPage.createNewUser(longUsername, "b", "User");

        usersPage.closeModal();

        assertThat(usersPage.deleteButton(longUsername), displayed());

        assertThat(usersPage.getTable(), containsText(longUsername));
        usersPage.deleteUser(longUsername);

        assertThat(usersPage.getTable(), not(containsText(longUsername)));
    }

    @Test
    public void testLogOutAndLogInWithNewUser() {
        signUpAndLoginAs(aNewUser);
    }
}
