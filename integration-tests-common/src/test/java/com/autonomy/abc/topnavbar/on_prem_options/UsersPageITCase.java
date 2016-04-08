package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.base.SOTestBase;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.users.UserNotCreatedException;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.users.UsersPage;
import com.autonomy.abc.shared.UserTestHelper;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.modalIsDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;


public class UsersPageITCase extends SOTestBase {
	private final int defaultNumberOfUsers = isHosted() ? 0 : 1;
	private final NewUser aNewUser;
	private final NewUser newUser2;
	private final UserTestHelper helper;

	private UsersPage usersPage;
	private UserService<?> userService;

	public UsersPageITCase(final TestConfig config) {
		super(config);
		aNewUser = config.getNewUser("james");
		newUser2 = config.getNewUser("john");
		helper = new UserTestHelper(getApplication(), getConfig());
	}

	@Before
	public void setUp() {
		userService = getApplication().userService();
		usersPage = userService.goToUsers();
		userService.deleteOtherUsers();
	}

	@After
	public void emailTearDown() {
		if (hasSetUp() && isHosted()) {
			helper.deleteEmails(getMainSession());
		}
	}

	@After
	public void userTearDown() {
		SOTearDown.USERS.tearDown(this);
	}

	@Test
	public void testDeleteAllUsers() {
		final int initialNumberOfUsers = usersPage.countNumberOfUsers();
		usersPage.createUserButton().click();
		assertThat(usersPage, modalIsDisplayed());
		User user = usersPage.addNewUser(aNewUser, Role.USER);
		User admin = usersPage.addNewUser(newUser2, Role.ADMIN);
		usersPage.userCreationModal().close();
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 2));

		this.helper.deleteAndVerify(admin);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers + 1));

		this.helper.deleteAndVerify(user);
		verifyThat(usersPage.countNumberOfUsers(), is(initialNumberOfUsers));

		usersPage.createUserButton().click();
		verifyThat(usersPage, modalIsDisplayed());
		usersPage.addNewUser(aNewUser, Role.USER);
		usersPage.addNewUser(newUser2, Role.ADMIN);
		usersPage.userCreationModal().close();
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
		this.helper.verifyUserAdded(newUserModal, original);

		try {
			usersPage.addNewUser(aNewUser, Role.USER);
		} catch (TimeoutException | UserNotCreatedException e) { /* Expected */}
		verifyDuplicateError(newUserModal);

		try {
			usersPage.addNewUser(getConfig().getNewUser("testAddDuplicateUser_james"), Role.USER);
		} catch (TimeoutException | UserNotCreatedException e) { /* Expected */}

		verifyDuplicateError(newUserModal);

		usersPage.userCreationModal().close();
		verifyThat(usersPage.countNumberOfUsers(), is(1 + defaultNumberOfUsers));
	}

	private void verifyDuplicateError(ModalView newUserModal) {
		Serializable expectedError;
		if (isHosted()) {
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
		this.helper.verifyUserAdded(newUserModal, admin);

		User user = usersPage.addNewUser(newUser2, Role.USER);
		this.helper.verifyUserAdded(newUserModal, user);

		usersPage.userCreationModal().close();
		List<String> usernames = usersPage.getUsernames();
		assertThat(usernames, hasItem(admin.getUsername()));
		assertThat(usersPage.getRoleOf(admin), is(Role.ADMIN));

		assertThat(usernames, hasItem(user.getUsername()));
		assertThat(usersPage.getRoleOf(user), is(Role.USER));
	}

	@Test
	public void testEditUserType() {
		User user = helper.singleSignUp(aNewUser);

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
		User user = helper.singleSignUp(aNewUser);

		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(user.getRole()));

		userService.changeRole(user, Role.NONE);
		assertThat(usersPage.roleLinkFor(user), displayed());
		assertThat(usersPage.getRoleOf(user), is(Role.NONE));

		Waits.waitForGritterToClear();

		this.helper.logoutAndNavigateToWebApp(getWindow());

		try {
			getApplication().loginService().login(user);
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
