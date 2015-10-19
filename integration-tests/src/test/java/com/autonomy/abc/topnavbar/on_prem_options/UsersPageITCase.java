package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.OPAppBody;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.users.UserService;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnhandledAlertException;

import java.net.MalformedURLException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class UsersPageITCase extends ABCTestBase {

	private static final String STUPIDLY_LONG_USERNAME = "StupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserName";

	public UsersPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private UsersPage usersPage;
	private UserService userService;
	
	@Before
	public void setUp() throws MalformedURLException, InterruptedException {
		Thread.sleep(5000);
		userService = getApplication().createUserService(getElementFactory());
		userService.goToUsers();
		usersPage = getElementFactory().getUsersPage();
		usersPage.deleteOtherUsers();
	}

	@Test
	public void testCreateUser() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", getDriver().findElements(By.cssSelector(".modal[aria-hidden='false']")).size() > 0);
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal has not opened", newUserModal.getText().startsWith("Create New Users"));

		usersPage.createButton().click();
		assertThat("Error message not shown", newUserModal.getText().contains("Error! Username must not be blank"));

		usersPage.addUsername("Andrew");
		usersPage.clearPasswords();
		usersPage.createButton().click();
		assertThat("Correct error message not shown", newUserModal.getText().contains("Error! Password must not be blank"));

		usersPage.addAndConfirmPassword("password", "wordpass");
		usersPage.createButton().click();
		assertThat("Correct error message not shown", newUserModal.getText().contains("Error! Password confirmation failed"));

		usersPage.createNewUser("Andrew", "qwerty", "Admin");
		assertThat("Completion message not shown", newUserModal.getText().contains("Done! User Andrew successfully created"));

		usersPage.closeModal();
		assertThat("Modal has not closed", !usersPage.getText().contains("Create New Users"));
	}

	@Test
	@Ignore
	public void testWontDeleteSelf() {
		//TODO don't have a login name on HSO
//		assertThat("Delete button not disabled", usersPage.isAttributePresent((usersPage.getUserRow(getLoginName()).findElement(By.cssSelector("button"))), "disabled"));
	}

	@Test
	public void testDeleteAllUsers() {
		final int initialNumberOfUsers = usersPage.countNumberOfUsers();
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("Paul", "w", "User");
		usersPage.createNewUser("Stephen", "w", "Admin");
		usersPage.closeModal();
		assertThat("Not all users are added", usersPage.countNumberOfUsers() == initialNumberOfUsers + 2);

		usersPage.deleteUser("Paul");
		assertThat("User not deleted", usersPage.countNumberOfUsers() == initialNumberOfUsers + 1);
		assertThat("Paul was not deleted", usersPage.getText().contains("User Paul successfully deleted"));

		usersPage.deleteUser("Stephen");
		assertThat("User not deleted", usersPage.countNumberOfUsers() == initialNumberOfUsers);
		assertThat("Stephen was not deleted", usersPage.getText().contains("User Stephen successfully deleted"));

		usersPage.createUserButton().click();
		usersPage.createNewUser("Paul", "w", "User");
		usersPage.createNewUser("Stephen", "w", "Admin");
		usersPage.closeModal();

		usersPage.deleteOtherUsers();
		assertThat("Not all users are deleted", usersPage.countNumberOfUsers() == 1);
	}

	@Test
	public void testAddDuplicateUser() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.addUsername("Felix");
		usersPage.addAndConfirmPassword("password", "password");
		usersPage.createButton().click();
		usersPage.loadOrFadeWait();
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		assertThat("Completion message not shown", newUserModal.getText().contains("Done! User Felix successfully created"));

		usersPage.addUsername("Felix");
		usersPage.addAndConfirmPassword("wordPass", "wordPass");
		usersPage.createButton().click();
		assertThat("Completion message not shown", newUserModal.getText().contains("Error! User exists!"));

		usersPage.closeModal();
		assertThat("Wrong number of users created", usersPage.countNumberOfUsers() == 2);
	}

	@Test
	public void testUserDetails() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("William", "poiuy", "Admin");
		final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
		assertThat("Completion message not shown", newUserModal.getText().contains("Done! User William successfully created"));

		usersPage.createNewUser("Henri", "lkjh", "User");
		assertThat("Completion message not shown", newUserModal.getText().contains("Done! User Henri successfully created"));

		usersPage.closeModal();
		assertThat("User not in table", usersPage.getTable().getText().contains("William"));
		assertThat("User type incorrect", usersPage.getTableUserTypeLink("William").getText().equals("Admin"));

		assertThat("User not in table", usersPage.getTable().getText().contains("Henri"));
		assertThat("User type incorrect", usersPage.getTableUserTypeLink("Henri").getText().equals("User"));
	}

	@Test
	public void testEditUserPassword() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("alan", "q", "Admin");
		usersPage.closeModal();

		usersPage.getTableUserPasswordLink("alan").click();
		usersPage.getTableUserPasswordBox("alan").clear();
		usersPage.getUserRow("alan").findElement(By.cssSelector(".editable-submit")).click();
		assertThat("Blank password error message is missing", usersPage.getUserRow("alan").getText().contains("Password must not be blank"));
		assertThat("Edit password box should still be visible", usersPage.getTableUserPasswordBox("alan").isDisplayed());

		usersPage.getTableUserPasswordBox("alan").sendKeys("nala");
		usersPage.getUserRow("alan").findElement(By.cssSelector(".editable-submit")).click();
		assertThat("Edit password Link should now be visible", usersPage.getTableUserPasswordLink("alan").isDisplayed());
	}

	@Test
	public void testEditUserType() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("Bella", "a", "Admin");
		usersPage.closeModal();

		usersPage.getTableUserTypeLink("Bella").click();
		usersPage.selectTableUserType("Bella", "User");
		usersPage.getUserRow("Bella").findElement(By.cssSelector(".editable-cancel")).click();
		assertThat("Edit type link should be visible", usersPage.getTableUserTypeLink("Bella").isDisplayed());
		assertThat("User type incorrect: Type change not cancelled", usersPage.getTableUserTypeLink("Bella").getText().equals("Admin"));

		usersPage.getTableUserTypeLink("Bella").click();
		usersPage.selectTableUserType("Bella", "User");
		usersPage.getUserRow("Bella").findElement(By.cssSelector(".editable-submit")).click();
		assertThat("Edit type link should be visible", usersPage.getTableUserTypeLink("Bella").isDisplayed());
		assertThat("User type incorrect: Type change not cancelled", usersPage.getTableUserTypeLink("Bella").getText().equals("User"));

		usersPage.getTableUserTypeLink("Bella").click();
		usersPage.selectTableUserType("Bella", "Admin");
		usersPage.getUserRow("Bella").findElement(By.cssSelector(".editable-submit")).click();
		usersPage.loadOrFadeWait();
		assertThat("Edit type link should be visible", usersPage.getTableUserTypeLink("Bella").isDisplayed());
		assertThat("User type incorrect: Type change not cancelled", usersPage.getTableUserTypeLink("Bella").getText().equals("Admin"));

		usersPage.getTableUserTypeLink("Bella").click();
		usersPage.selectTableUserType("Bella", "None");
		usersPage.getUserRow("Bella").findElement(By.cssSelector(".editable-submit")).click();
		assertThat("Edit type link should be visible", usersPage.getTableUserTypeLink("Bella").isDisplayed());
		assertThat("User type incorrect: Type change not cancelled", usersPage.getTableUserTypeLink("Bella").getText().equals("None"));
	}

	@Test
	public void testAddStupidlyLongUsername() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser(STUPIDLY_LONG_USERNAME, "b", "User");
		usersPage.closeModal();

		assertThat("Long username not added to the table", usersPage.getTable().getText().contains(STUPIDLY_LONG_USERNAME));
		assertThat("", usersPage.deleteButton(STUPIDLY_LONG_USERNAME).isDisplayed());

		usersPage.deleteUser(STUPIDLY_LONG_USERNAME);
		assertThat("Long username not removed from the table", !usersPage.getTable().getText().contains(STUPIDLY_LONG_USERNAME));
	}

	@Test
	public void testLogOutAndLogInWithNewUser() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", getDriver().findElements(By.cssSelector(".modal[aria-hidden='false']")).size() > 0);
		usersPage.createNewUser("James", "b", "User");
		usersPage.closeModal();

		body.getTopNavBar().logOut();
		UserService us = getApplication().createUserService(getElementFactory());
		userService.login(new User("James", "b", "email"));
		usersPage.loadOrFadeWait();
		assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));
	}

	@Test
	public void testChangeOfPasswordWorksOnLogin() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("James", "b", "User");
		usersPage.closeModal();

		usersPage.changePassword("James", "d");

		body.getTopNavBar().logOut();
		userService.login(new User("James", "d", "email"));
		usersPage.loadOrFadeWait();
		assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));
	}

	@Test
	public void testCreateUserPermissionNoneAndTestLogin() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("Norman", "n", "User");
		usersPage.closeModal();
		assertThat("Edit type link should be visible", usersPage.getTableUserTypeLink("Norman").isDisplayed());
		assertThat("User type incorrect: Type change not cancelled", usersPage.getTableUserTypeLink("Norman").getText().equals("User"));

		usersPage.getTableUserTypeLink("Norman").click();
		usersPage.selectTableUserType("Norman", "None");
		usersPage.getUserRow("Norman").findElement(By.cssSelector(".editable-submit")).click();
		usersPage.loadOrFadeWait();
		assertThat("Edit type link should be visible", usersPage.getTableUserTypeLink("Norman").isDisplayed());
		assertThat("User type incorrect: Type change not cancelled", usersPage.getTableUserTypeLink("Norman").getText().equals("None"));

		body.getTopNavBar().logOut();
		userService.login(new User("Norman", "n", "email"));
		getElementFactory().getLoginPage();
        assertThat("Wrong/no error message displayed", getDriver().findElement(By.xpath("//*")).getText(),containsString("Please check your username and password."));
        assertThat("URL wrong",getDriver().getCurrentUrl(),containsString("login"));
	}

	@Test
	public void testAnyUserCanNotAccessConfigPage() {
		String baseUrl = config.getWebappUrl();
		baseUrl = baseUrl.replace("/p/","/config");
		getDriver().get(baseUrl);
		usersPage.loadOrFadeWait();
		assertTrue("Users are not allowed to access the config page", getDriver().findElement(By.cssSelector("body")).getText().contains("Authentication Failed"));
	}

	@Test
	public void testUserCannotAccessUsersPageOrSettingsPage() {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("James", "b", "User");
		usersPage.closeModal();

		body.getTopNavBar().logOut();
		userService.login(new User("James", "b", "email"));
		usersPage.loadOrFadeWait();
		assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));

		getDriver().get(config.getWebappUrl() + "settings");
		usersPage.loadOrFadeWait();
		assertFalse(getDriver().getCurrentUrl().contains("settings"));
		Assert.assertTrue(getDriver().getCurrentUrl().contains("overview"));

		getDriver().get(config.getWebappUrl() + "users");
		usersPage.loadOrFadeWait();
		assertFalse(getDriver().getCurrentUrl().contains("users"));
		Assert.assertTrue(getDriver().getCurrentUrl().contains("overview"));
	}

	@Test
	public void testXmlHttpRequestToUserConfigBlockedForInadequatePermissions() throws UnhandledAlertException {
		usersPage.createUserButton().click();
		assertTrue("Create user modal is not showing", usersPage.isModalShowing());
		usersPage.createNewUser("James", "b", "User");
		usersPage.closeModal();
		body.getTopNavBar().logOut();

		userService.login(new User("James", "b", "email"));
		usersPage.loadOrFadeWait();
		assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));

		final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function(xhr) {$('body').attr('data-status', xhr.status);});");
		usersPage.loadOrFadeWait();
		Assert.assertTrue(getDriver().findElement(By.cssSelector("body")).getAttribute("data-status").contains("403"));

		body = new OPAppBody(getDriver());
		body.getTopNavBar().logOut();

		userService.login(new User("Richard", "q", "email"));
		usersPage.loadOrFadeWait();
		assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));

		executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function() {alert(\"error\");});");
		usersPage.loadOrFadeWait();
		assertFalse(usersPage.isAlertPresent());
	}
}
