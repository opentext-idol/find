package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.shared.UsersPageTestBase;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Editable;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class UsersPageOnPremITCase extends UsersPageTestBase<NewUser> {
    private IdolUsersPage usersPage;

    public UsersPageOnPremITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void pageSetUp() {
        usersPage = (IdolUsersPage) userService.getUsersPage();
    }

    @Test
    public void testAnyUserCanNotAccessConfigPage() {
        signUpAndLoginAs(aNewUser);

        String baseUrl = getAppUrl();
        baseUrl = baseUrl.replace("/p/","/config");
        getDriver().get(baseUrl);
        Waits.loadOrFadeWait();
        assertThat("Users are not allowed to access the config page", getDriver().findElement(By.tagName("body")), containsText("Authentication Failed"));
    }

    @Test
    public void testUserCannotAccessUsersPageOrSettingsPage() {
        signUpAndLoginAs(aNewUser);

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
    public void testChangeOfPasswordWorksOnLogin() {
        User initialUser = singleSignUp();
        User updatedUser = usersPage.replaceAuthFor(initialUser, new IdolIsoReplacementAuth("bob"));

        logoutAndNavigateToWebApp();
        loginAs(initialUser);
        Waits.loadOrFadeWait();
        assertThat("old password does not work", getWindow(), urlContains("login"));

        loginAs(updatedUser);
        Waits.loadOrFadeWait();
        assertThat("new password works", getWindow(), url(not(containsString("login"))));
    }

    @Test
    public void testEditUserPassword() {
        assumeThat(getConfig().getType(), is(ApplicationType.ON_PREM));
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
        final IdolUserCreationModal newUserModal = usersPage.userCreationModal();
        verifyThat(newUserModal, hasTextThat(startsWith("Create New Users")));

        newUserModal.createButton().click();
        verifyThat(newUserModal, containsText("Error! Username must not be blank"));

        newUserModal.usernameInput().setValue("Andrew");
        newUserModal.passwordInput().clear();
        newUserModal.passwordConfirmInput().clear();
        newUserModal.createButton().click();
        verifyThat(newUserModal, containsText("Error! Password must not be blank"));

        newUserModal.passwordInput().setValue("password");
        newUserModal.passwordConfirmInput().setValue("wordpass");
        newUserModal.createButton().click();
        verifyThat(newUserModal, containsText("Error! Password confirmation failed"));

        newUserModal.usernameInput().setValue("Andrew");
        newUserModal.passwordInput().setValue("qwerty");
        newUserModal.passwordConfirmInput().setValue("qwerty");
        newUserModal.selectRole(Role.ADMIN);
        newUserModal.createUser();
        verifyThat(newUserModal, containsText("Done! User Andrew successfully created"));

        newUserModal.close();
        verifyThat(usersPage, not(containsText("Create New Users")));
    }

    @Test
    //TO BE MOVED BACK TO COMMON IF FUNCTIONALITY IS IMPLEMENTED
    public void testWontDeleteSelf() {
        assertThat(usersPage.deleteButton(getLoginService().getCurrentUser()), hasClass("not-clickable"));
    }

    @Test
    public void testXmlHttpRequestToUserConfigBlockedForInadequatePermissions() throws UnhandledAlertException {
        signUpAndLoginAs(aNewUser);

        final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function(xhr) {$('body').attr('data-status', xhr.status);});");
        Waits.loadOrFadeWait();
        Assert.assertTrue(getDriver().findElement(By.cssSelector("body")).getAttribute("data-status").contains("403"));

        logoutAndNavigateToWebApp();
        loginAs(getConfig().getDefaultUser());
        Waits.loadOrFadeWait();
        assertThat(getWindow(), url(not(containsString("login"))));

        executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function() {alert(\"error\");});");
        Waits.loadOrFadeWait();
        assertThat(isAlertPresent(), is(false));
    }

    private boolean isAlertPresent() {
        try {
            getDriver().switchTo().alert();
            return true;
        } catch (final NoAlertPresentException ex) {
            return false;
        }
    }

    @Test
    public void testLogOutAndLogInWithNewUser() {
        signUpAndLoginAs(aNewUser);
    }

    @Test
    public void testAddStupidlyLongUsername() {
        final String longUsername = StringUtils.repeat("a", 100);
        verifyCreateDeleteInTable(new IdolIsoNewUser(longUsername, "b"));
    }
}
