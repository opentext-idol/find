package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.users.UserNotCreatedException;
import com.autonomy.abc.selenium.util.PageUtil;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.modalIsDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;


public class UsersPageITCase extends UsersPageTestBase {
	public UsersPageITCase(final TestConfig config) {
		super(config);
	}

	@Test
	public void testDeleteAllUsers() {
		final int initialNumberOfUsers = usersPage.countNumberOfUsers();
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User user = usersPage.addNewUser(aNewUser, Role.USER);
		User admin = usersPage.addNewUser(newUser2, Role.ADMIN);
		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 2));

		deleteAndVerify(admin);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 1));

		deleteAndVerify(user);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers));

		usersPage.createUserButton().click();
		verifyThat(PageUtil.isModalShowing(getDriver()), is(true));
		usersPage.addNewUser(aNewUser, Role.USER);
		usersPage.addNewUser(newUser2, Role.ADMIN);
		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 2));

		userService.deleteOtherUsers();
		verifyThat("All users are deleted", usersPage.countNumberOfUsers(), is(defaultNumberOfUsers));
	}

	@Test
	public void testAddDuplicateUser() {
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User original = usersPage.addNewUser(aNewUser, Role.USER);
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		verifyUserAdded(newUserModal, original);

		try {
			usersPage.addNewUser(aNewUser, Role.USER);
		} catch (TimeoutException | UserNotCreatedException e) { /* Expected */}
		verifyDuplicateError(newUserModal);

		try {
			usersPage.addNewUser(getConfig().getNewUser("testAddDuplicateUser_james"), Role.USER);
		} catch (TimeoutException | UserNotCreatedException e) { /* Expected */}

		verifyDuplicateError(newUserModal);

		usersPage.closeModal();
		verifyThat(usersPage.countNumberOfUsers(), is(1 + defaultNumberOfUsers));
	}

	private void verifyDuplicateError(ModalView newUserModal) {
		String expectedError;
		if (getConfig().getType().equals(ApplicationType.HOSTED)) {
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

		User admin = usersPage.addNewUser(aNewUser, Role.ADMIN);
		verifyUserAdded(newUserModal, admin);

		User user = usersPage.addNewUser(newUser2, Role.USER);
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
		Waits.loadOrFadeWait();
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
		userService.changeRole(user, role);

		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(role));
	}

	@Test
	public void testCreateUserPermissionNoneAndTestLogin() throws InterruptedException {
		User user = singleSignUp();

		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(user.getRole()));

		userService.changeRole(user, Role.NONE);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.NONE));

		Waits.waitForGritterToClear();

		logoutAndNavigateToWebApp();

		try {
			loginAs(user);
		} catch (NoSuchElementException e) {
			try {
				if (getDriver().findElement(By.linkText("Google")).isDisplayed()) {
					fail("Still on login page");
				}
			} catch (NoSuchElementException f){
				/* There shouldn't be Google on the page, so it SHOULD fail */
			}
		}
		verifyThat("Directed to 401 page", getDriver().findElement(By.tagName("body")), containsText("401"));
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
		} catch (TimeoutException | UserNotCreatedException e) {
			/* Expected */
		}

		verifyThat(usersPage.getUserCountInTitle(), is(2));

		userService.deleteUser(user2);
		verifyThat(usersPage.getUserCountInTitle(), is(1));

		userService.deleteUser(user1);
		Waits.loadOrFadeWait();
		verifyThat(usersPage.getUserCountInTitle(), is(0));
	}
}
