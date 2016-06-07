package com.autonomy.abc.users;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.fixtures.UserTearDownStrategy;
import com.autonomy.abc.selenium.auth.IdolIsoNewUser;
import com.autonomy.abc.selenium.auth.IdolIsoReplacementAuth;
import com.autonomy.abc.selenium.users.IdolIsoUserService;
import com.autonomy.abc.selenium.users.IdolUserCreationModal;
import com.autonomy.abc.selenium.users.IdolUsersPage;
import com.autonomy.abc.shared.UserTestHelper;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Editable;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

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

public class UsersPageOnPremITCase extends IdolIsoTestBase {
    private final NewUser aNewUser;
    private UserTestHelper helper;

    private IdolUsersPage usersPage;

    public UsersPageOnPremITCase(TestConfig config) {
        super(config);
        aNewUser = config.getNewUser("james");
    }

    @Before
    public void setUp() {
        helper = new UserTestHelper(getApplication(), getConfig());
        IdolIsoUserService userService = getApplication().userService();
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
        new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationsDisappear());
    }

    @After
    public void userTearDown() {
        new UserTearDownStrategy(getInitialUser()).tearDown(this);
    }

    @Test
    public void testChangeOfPasswordWorksOnLogin() {
        User initialUser = helper.singleSignUp(aNewUser);
        User updatedUser = usersPage.replaceAuthFor(initialUser, new IdolIsoReplacementAuth("bob"));

        this.helper.logoutAndNavigateToWebApp(getWindow());
        getApplication().loginService().login(initialUser);
        Waits.loadOrFadeWait();
        assertThat("old password does not work", getWindow(), urlContains("login"));

        getApplication().loginService().login(updatedUser);
        Waits.loadOrFadeWait();
        assertThat("new password works", getWindow(), url(not(containsString("login"))));
    }

    @Test
    public void testEditUserPassword() {
        assumeThat(getConfig().getType(), is(ApplicationType.ON_PREM));
        User user = helper.singleSignUp(aNewUser);

        Editable passwordBox = usersPage.getUserRow(user).passwordBox();
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
        verifyThat(newUserModal, containsText("must not be blank"));

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
        WebElement notification = new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationAppears());
        verifyThat(notification, hasTextThat(containsString("Created user Andrew")));

        newUserModal.close();
        verifyThat(usersPage, not(containsText("Create New Users")));
    }

    @Test
    public void testWontDeleteSelf() {
        User self = getApplication().loginService().getCurrentUser();
        assertThat(usersPage.getUserRow(self).canDeleteUser(),is(false));
    }

    @Test
    public void testLogOutAndLogInWithNewUser() {
        this.helper.signUpAndLoginAs(aNewUser, getWindow());
    }

    @Test
    public void testAddStupidlyLongUsername() {
        final String longUsername = StringUtils.repeat("a", 100);
        this.helper.verifyCreateDeleteInTable(new IdolIsoNewUser(longUsername, "bbb"));
    }
}
