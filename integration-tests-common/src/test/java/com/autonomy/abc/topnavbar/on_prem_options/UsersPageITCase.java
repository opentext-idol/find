package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
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
	private int defaultNumberOfUsers = (getConfig().getType() == ApplicationType.HOSTED) ? 0 : 1;

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

		try {
			loginAs(user);
		} catch (TimeoutException e) { /* Probably because of the sessions you're already logged in */ }

		getElementFactory().getPromotionsPage();
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
		verifyThat("All users are deleted", usersPage.countNumberOfUsers(), is(defaultNumberOfUsers));
	}

	private void verifyUserAdded(ModalView newUserModal, User user){
		if(getConfig().getType().equals(ApplicationType.ON_PREM)){
			verifyThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
		}

		//Hosted notifications are dealt with within the sign up method and there is no real way to ensure that a user's been created at the moment
	}

	@Test
	public void testAddDuplicateUser() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User original = aNewUser.signUpAs(Role.USER, usersPage);
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		verifyUserAdded(newUserModal, original);

		try {
			aNewUser.signUpAs(Role.USER, usersPage);
		} catch (TimeoutException e) { /* Expected */}
		verifyThat(newUserModal, containsText("Error! User exists!"));

		try {
			config.getNewUser("testAddDuplicateUser_james").signUpAs(Role.USER, usersPage);
		} catch (TimeoutException e) { /* Expected */}

		verifyThat(newUserModal, containsText("Error! User exists!"));

		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(2 - defaultNumberOfUsers));
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

	@Test
	public void testDisablingAndDeletingUser(){
		User user = userService.createNewUser(aNewUser, Role.USER);

		userService.changeRole(user, Role.NONE);
		verifyThat(usersPage.getRoleOf(user), is(Role.NONE));

		userService.deleteUser(user);
		verifyThat(usersPage.getUsernames(), not(hasItem(user.getUsername())));
	}

	@Test
	public void testAddUser(){
		User user = singleSignUp();
		verifyUserShowsUpInTable(user, Status.PENDING);
	}

	private void verifyUserShowsUpInTable(User user, Status expectedStatus){
		verifyThat(usersPage.getUsernames(), CoreMatchers.hasItem(user.getUsername()));
		verifyThat(usersPage.getRoleOf(user), is(Role.USER));

		if(getConfig().getType().equals(ApplicationType.HOSTED)){
			HSOUsersPage usersPage = (HSOUsersPage) this.usersPage;
			HSOUser hsoUser = (HSOUser) user;

			verifyThat(usersPage.getEmailOf(user), is(hsoUser.getEmail()));
			verifyThat(usersPage.getStatusOf(user), is(expectedStatus));
		}
	}

	@Test
	public void testUserSearch(){
		NewUser newUser3 = getConfig().getNewUser("bob");

		String[] addedUsers = new String[3];

		addedUsers[0] = userService.createNewUser(aNewUser, Role.ADMIN).getUsername();
		addedUsers[1] = userService.createNewUser(newUser2, Role.ADMIN).getUsername();
		addedUsers[2] = userService.createNewUser(newUser3, Role.ADMIN).getUsername();

		FormInput searchFilter = usersPage.userSearchFilter();

		searchFilter.setValue("hodtestqa");

		verifyThat(usersPage.getUsernames(), containsInAnyOrder(addedUsers));

		searchFilter.setValue("newuser1");

		List<String> usernames = usersPage.getUsernames();
		verifyThat(usernames, hasItem(addedUsers[0]));
		verifyThat(usernames, not(hasItem(addedUsers[1])));
		verifyThat(usernames, not(hasItem(addedUsers[2])));

		searchFilter.setValue("j");

		usernames = usersPage.getUsernames();
		verifyThat(usernames, hasItem(addedUsers[0]));
		verifyThat(usernames, hasItem(addedUsers[1]));
		verifyThat(usernames, not(hasItem(addedUsers[2])));

		searchFilter.setValue("bob");

		usernames = usersPage.getUsernames();
		verifyThat(usernames, not(hasItem(addedUsers[0])));
		verifyThat(usernames, not(hasItem(addedUsers[1])));
		verifyThat(usernames, hasItem(addedUsers[2]));
	}

	@Test
	public void testUserFilter(){
		LoggerFactory.getLogger(UsersPageITCase.class).warn("CANNOT FILTER BY 'NONE' - NEEDS TO BE UNCOMMENTED WHEN WORKING");

		NewUser newUser3 = getConfig().getNewUser("bob");

		String[] addedUsers = new String[2];

		addedUsers[0] = userService.createNewUser(aNewUser, Role.ADMIN).getUsername();
		addedUsers[1] = userService.createNewUser(newUser2, Role.USER).getUsername();
//		addedUsers[2] = userService.createNewUser(newUser3, Role.USER).getUsername();

		Dropdown dropdown = usersPage.userRoleFilter();

		verifyThat(usersPage.getUsernames(), containsInAnyOrder(addedUsers));

		dropdown.select("Admin");

		List<String> usernames = usersPage.getUsernames();
		verifyThat(usernames, hasItem(addedUsers[0]));
		verifyThat(usernames, not(hasItem(addedUsers[1])));
//		verifyThat(usernames, not(hasItem(addedUsers[2])));

		dropdown.select("User");

		usernames = usersPage.getUsernames();
		verifyThat(usernames, not(hasItem(addedUsers[0])));
		verifyThat(usernames, hasItem(addedUsers[1]));
//		verifyThat(usernames, not(hasItem(addedUsers[2])));

//		dropdown.select("None");
//
//		usernames = usersPage.getUsernames();
//		verifyThat(usernames, not(hasItem(addedUsers[0])));
//		verifyThat(usernames, not(hasItem(addedUsers[1])));
//		verifyThat(usernames, hasItem(addedUsers[2]));
	}

	@Test
	public void testUserCount(){
		verifyThat(usersPage.getUserCountInTitle(), is(0));

		User user1 = userService.createNewUser(aNewUser, Role.ADMIN);
		verifyThat(usersPage.getUserCountInTitle(), is(1));

		User user2 = userService.createNewUser(newUser2, Role.ADMIN);
		verifyThat(usersPage.getUserCountInTitle(), is(2));

		try {
			userService.createNewUser(aNewUser, Role.ADMIN);
		} catch (TimeoutException | HSONewUser.UserNotCreatedException e) {
			/* Expected */
			usersPage.closeModal();
		}

		verifyThat(usersPage.getUserCountInTitle(), is(2));

		userService.deleteUser(user2);
		verifyThat(usersPage.getUserCountInTitle(), is(1));

		userService.deleteUser(user1);
		usersPage.loadOrFadeWait();
		verifyThat(usersPage.getUserCountInTitle(), is(0));
	}
}
