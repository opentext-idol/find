package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;


public class UsersPageITCase extends ABCTestBase {
	public UsersPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private final NewUser aNewUser = config.getNewUser("james");
	private final NewUser newUser2 = config.getNewUser("john");
	private UsersPage usersPage;
	private UserService userService;
	private int defaultNoUsers = (getConfig().getType() == ApplicationType.HOSTED) ? 0 : 1;

	@Before
	public void setUp() throws MalformedURLException, InterruptedException {
		userService = getApplication().createUserService(getElementFactory());
		usersPage = userService.goToUsers();
		userService.deleteOtherUsers();
	}

	private User singleSignUp() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		User user = aNewUser.signUpAs(Role.USER, usersPage);
//		assertThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
		verifyUserAdded(newUserModal, user);
		usersPage.closeModal();
		return user;
	}

	private void signUpAndLoginAs(NewUser newUser) {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());

		User user = newUser.signUpAs(Role.USER, usersPage);
		usersPage.closeModal();

		try {
			usersPage.waitForGritterToClear();
		} catch (InterruptedException e) { /**/ }

		logout();

		getDriver().get(getConfig().getWebappUrl());

		loginAs(user);
		assertThat(getDriver().getCurrentUrl(), not(containsString("login")));
	}

	private void deleteAndVerify(User user) {
		usersPage.deleteUser(user.getUsername());
		if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
			verifyThat(usersPage, containsText("User " + user.getUsername() + " successfully deleted"));
		} else {
			new WebDriverWait(getDriver(),10).withMessage("User " + user.getUsername() + " not successfully deleted").until(GritterNotice.notificationContaining("Deleted user " + user.getUsername()));
		}
	}

	@Test
	public void testCreateUser() {
		// TODO: split into HS/OP?
		assumeThat(config.getType(), is(ApplicationType.ON_PREM));

		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		verifyThat(newUserModal, hasTextThat(startsWith("Create New Users")));

		usersPage.createButton().click();
		verifyThat(newUserModal, containsText("Error! Username must not be blank"));

		usersPage.addUsername("Andrew");
		usersPage.clearPasswords();
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
	public void testWontDeleteSelf() {
		assertThat(usersPage.deleteButton(getCurrentUser().getUsername()), disabled());
	}

	@Test
	public void testDeleteAllUsers() {
		final int initialNumberOfUsers = usersPage.countNumberOfUsers();
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User user = aNewUser.signUpAs(Role.USER, usersPage);
		usersPage.loadOrFadeWait();
		User admin = newUser2.signUpAs(Role.ADMIN, usersPage);
		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 2));

		deleteAndVerify(admin);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 1));

		deleteAndVerify(user);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers));

		usersPage.createUserButton().click();
		verifyThat(usersPage.isModalShowing(), is(true));
		aNewUser.signUpAs(Role.USER, usersPage);
		newUser2.signUpAs(Role.ADMIN, usersPage);
		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 2));

		userService.deleteOtherUsers();
		verifyThat("All users are deleted", usersPage.countNumberOfUsers(), is(defaultNoUsers));
	}

	private void verifyUserAdded(ModalView newUserModal, User user){
		if(getConfig().getType().equals(ApplicationType.ON_PREM)){
			verifyThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
//		} else {
//			new WebDriverWait(getDriver(),10).withMessage("User not added").until(GritterNotice.notificationContaining("Created user " + user.getUsername()));
		}
	}

	@Test
	public void testAddDuplicateUser() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User original = aNewUser.signUpAs(Role.USER, usersPage);
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		verifyUserAdded(newUserModal, original);

		aNewUser.signUpAs(Role.USER, usersPage);
		verifyThat(newUserModal, containsText("Error! User exists!"));

		config.getNewUser("testAddDuplicateUser_james").signUpAs(Role.USER, usersPage);
		verifyThat(newUserModal, containsText("Error! User exists!"));

		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(2 - defaultNoUsers));
	}

	@Test
	public void testUserDetails() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());

		User admin = aNewUser.signUpAs(Role.ADMIN, usersPage);
		verifyUserAdded(newUserModal, admin);

		User user = newUser2.signUpAs(Role.USER, usersPage);
		verifyUserAdded(newUserModal, user);

		usersPage.closeModal();
		List<String> usernames = usersPage.getUsernames();
		assertThat(usernames, hasItem(admin.getUsername()));
		assertThat(usersPage.getRoleOf(admin), is(Role.ADMIN));

		assertThat(usernames, hasItem(user.getUsername()));
		assertThat(usersPage.getRoleOf(user), is(Role.USER));
	}

	@Test
	public void testEditUserPassword() {
		// OP/HS split?
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
	public void testEditUserType() {
		User user = singleSignUp();

		usersPage.roleLinkFor(user).click();
		usersPage.setRoleValueFor(user, Role.ADMIN);
		usersPage.cancelPendingEditFor(user);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(user.getRole()));

		userService.changeRole(user, Role.USER);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.USER));

		userService.changeRole(user, Role.ADMIN);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.ADMIN));

		userService.changeRole(user, Role.NONE);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.NONE));
	}

	@Test
	public void testAddStupidlyLongUsername() {
		assumeThat(config.getType(), is(ApplicationType.ON_PREM));
		final String longUsername = StringUtils.repeat("a", 100);

		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		usersPage.createNewUser(longUsername, "b", "User");
		usersPage.closeModal();

		assertThat(usersPage.getTable(), containsText(longUsername));
		assertThat(usersPage.deleteButton(longUsername), displayed());

		usersPage.deleteUser(longUsername);
		assertThat(usersPage.getTable(), not(containsText(longUsername)));
	}

	@Test
	public void testLogOutAndLogInWithNewUser() {
		signUpAndLoginAs(aNewUser);
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
	public void testCreateUserPermissionNoneAndTestLogin() throws InterruptedException {
		User user = singleSignUp();

		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(user.getRole()));

		userService.changeRole(user, Role.NONE);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.NONE));

		usersPage.waitForGritterToClear();

		logoutAndNavigateToWebApp();
		loginAs(user);
		getElementFactory().getLoginPage();
        assertThat(getDriver().findElement(By.xpath("//*")), containsText("Please check your username and password."));
        assertThat(getDriver().getCurrentUrl(), containsString("login"));
	}

	private void logoutAndNavigateToWebApp(){
		logout();
		getDriver().get(getConfig().getWebappUrl());
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
}
