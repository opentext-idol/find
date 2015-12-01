package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.users.HSONewUser;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.slf4j.LoggerFactory;

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


public class UsersPageITCase extends UsersPageTestBase {
	public UsersPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
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
		User user = aNewUser.signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
		usersPage.loadOrFadeWait();
		User admin = newUser2.signUpAs(Role.ADMIN, usersPage, config.getWebDriverFactory());
		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 2));

		deleteAndVerify(admin);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 1));

		deleteAndVerify(user);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers));

		usersPage.createUserButton().click();
		verifyThat(usersPage.isModalShowing(), is(true));
		aNewUser.signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
		newUser2.signUpAs(Role.ADMIN, usersPage, config.getWebDriverFactory());
		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 2));

		userService.deleteOtherUsers();
		verifyThat("All users are deleted", usersPage.countNumberOfUsers(), is(defaultNumberOfUsers));
	}

	@Test
	public void testAddDuplicateUser() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User original = aNewUser.signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		verifyUserAdded(newUserModal, original);

		try {
			aNewUser.signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
		} catch (TimeoutException | HSONewUser.UserNotCreatedException e) { /* Expected */}
		verifyThat(newUserModal, containsText("Error! User exists!"));

		try {
			config.getNewUser("testAddDuplicateUser_james").signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
		} catch (TimeoutException | HSONewUser.UserNotCreatedException e) { /* Expected */}

		verifyThat(newUserModal, containsText("Error! User exists!"));

		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(2 - numberOfUsersExpected));
	}

	@Test
	public void testUserDetails() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());

		User admin = aNewUser.signUpAs(Role.ADMIN, usersPage, config.getWebDriverFactory());
		verifyUserAdded(newUserModal, admin);

		User user = newUser2.signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
		verifyUserAdded(newUserModal, user);

		usersPage.closeModal();
		List<String> usernames = usersPage.getUsernames();
		assertThat(usernames, hasItem(admin.getUsername()));
		assertThat(usersPage.getRoleOf(admin), is(Role.ADMIN));

		assertThat(usernames, hasItem(user.getUsername()));
		assertThat(usersPage.getRoleOf(user), is(Role.USER));
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
		final String longUsername = StringUtils.repeat("a", 100);

		if(getConfig().getType().equals(ApplicationType.ON_PREM)) {
			usersPage.createUserButton().click();
			assertThat(usersPage, modalIsDisplayed());

			usersPage.createNewUser(longUsername, "b", "User");

			usersPage.closeModal();

			assertThat(usersPage.deleteButton(longUsername), displayed());
		} else {
			userService.createNewUser(new HSONewUser(longUsername, "hodtestqa401+longusername@gmail.com"), Role.ADMIN, config.getWebDriverFactory());
		}

		assertThat(usersPage.getTable(), containsText(longUsername));

		usersPage.deleteUser(longUsername);
		assertThat(usersPage.getTable(), not(containsText(longUsername)));
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
		User user = userService.createNewUser(aNewUser, Role.USER, config.getWebDriverFactory());

		userService.changeRole(user, Role.NONE);
		verifyThat(usersPage.getRoleOf(user), is(Role.NONE));

		userService.deleteUser(user);
		verifyThat(usersPage.getUsernames(), not(hasItem(user.getUsername())));
	}

	@Test
	public void testUserSearch(){
		NewUser newUser3 = getConfig().getNewUser("bob");

		String[] addedUsers = new String[3];

		addedUsers[0] = userService.createNewUser(aNewUser, Role.ADMIN, config.getWebDriverFactory()).getUsername();
		addedUsers[1] = userService.createNewUser(newUser2, Role.ADMIN, config.getWebDriverFactory()).getUsername();
		addedUsers[2] = userService.createNewUser(newUser3, Role.ADMIN, config.getWebDriverFactory()).getUsername();

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

		addedUsers[0] = userService.createNewUser(aNewUser, Role.ADMIN, config.getWebDriverFactory()).getUsername();
		addedUsers[1] = userService.createNewUser(newUser2, Role.USER, config.getWebDriverFactory()).getUsername();
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

		User user1 = userService.createNewUser(aNewUser, Role.ADMIN, config.getWebDriverFactory());
		verifyThat(usersPage.getUserCountInTitle(), is(1));

		User user2 = userService.createNewUser(newUser2, Role.ADMIN, config.getWebDriverFactory());
		verifyThat(usersPage.getUserCountInTitle(), is(2));

		try {
			userService.createNewUser(aNewUser, Role.ADMIN, config.getWebDriverFactory());
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
