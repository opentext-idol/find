package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.selenium.util.Errors;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openqa.selenium.*;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.modalIsDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;


public class UsersPageITCase extends UsersPageTestBase {
	public UsersPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
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

	@Test
	public void testAddDuplicateUser() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User original = aNewUser.signUpAs(Role.USER, usersPage);
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		verifyUserAdded(newUserModal, original);

		try {
			aNewUser.signUpAs(Role.USER, usersPage);
		} catch (TimeoutException | HSONewUser.UserNotCreatedException e) { /* Expected */}
		verifyDuplicateError(newUserModal);

		try {
			config.getNewUser("testAddDuplicateUser_james").signUpAs(Role.USER, usersPage);
		} catch (TimeoutException | HSONewUser.UserNotCreatedException e) { /* Expected */}

		verifyDuplicateError(newUserModal);

		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(1 + defaultNumberOfUsers));
	}

	private void verifyDuplicateError(ModalView newUserModal) {
		String expectedError;
		if (config.getType().equals(ApplicationType.HOSTED)) {
			expectedError = Errors.User.DUPLICATE_EMAIL;
		} else {
			expectedError = Errors.User.DUPLICATE_USER;
		}
		verifyThat(newUserModal, containsText(expectedError));
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
	public void testEditUserType() {
		User user = singleSignUp();

		userService.changeRole(user, Role.ADMIN);
		usersPage.loadOrFadeWait();
		userService.changeRole(user, Role.USER);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.USER));

		selectSameRole(user);

		userService.changeRole(user, Role.ADMIN);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.ADMIN));

		userService.changeRole(user, Role.NONE);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.NONE));
	}

	private void selectSameRole(User user){
		Role role = user.getRole();

		if(getConfig().getType().equals(ApplicationType.ON_PREM)){
			userService.changeRole(user, role);
		} else {
			HSOUsersPage usersPage = (HSOUsersPage) this.usersPage;
			WebElement roleLink = usersPage.roleLinkFor(user);

			roleLink.click();
			roleLink.click();
		}

		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(role));
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

			assertThat(usersPage.getTable(), containsText(longUsername));
			usersPage.deleteUser(longUsername);
		} else {
			User user = userService.createNewUser(new HSONewUser(longUsername, "hodtestqa401+longusername@gmail.com"), Role.ADMIN, config.getWebDriverFactory());
			assertThat(usersPage.getTable(), containsText(longUsername));
			userService.deleteUser(user);
		}

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

		try {
			loginAs(user);
		} catch (NoSuchElementException e) {
			//If not hosted then login has totally failed
			assertThat(config.getType(), is(ApplicationType.HOSTED));

			try {
				if(getDriver().findElement(By.tagName("body")).getText().contains("401")){
					fail("401 Page error");
				}

				if(getDriver().findElement(By.linkText("Google")).isDisplayed()){
					fail("Still on login page");
				}
			} catch (NoSuchElementException f) {
				WebElement modal = getDriver().findElement(By.className("js-developer-form"));

				boolean validAccount = false;

				for (WebElement account : modal.findElements(By.className("list-group-item"))) {
					if (account.getText().equals(((HSOUser) user).getEmail())) {
						account.click();
						validAccount = true;
						break;
					}
				}

				if (!validAccount) {
					fail("Account could not be found in options");
				}
			}
		}

		getElementFactory().getLoginPage();
        assertThat(getDriver().findElement(By.xpath("//*")), containsText("Please check your username and password."));
        assertThat(getDriver().getCurrentUrl(), containsString("login"));
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
