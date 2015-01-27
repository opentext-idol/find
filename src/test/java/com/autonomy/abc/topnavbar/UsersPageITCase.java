package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.page.UsersPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;

import static org.hamcrest.MatcherAssert.assertThat;

public class UsersPageITCase extends ABCTestBase {

	private static String STUPIDLY_LONG_USERNAME = "StupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserName";

	public UsersPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private UsersPage usersPage;

	@Before
	public void setUp() throws MalformedURLException {
		usersPage = body.getUsersPage();
		usersPage.deleteOtherUsers();
	}

	@Test
	public void testCreateUser() {
		usersPage.createUserButton().click();
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
	public void testWontDeleteSelf() {
		assertThat("Delete button not disabled", usersPage.getUserRow(usersPage.getSignedInUserName()).findElements(By.cssSelector("button.disabled")).size() == 1);
	}

	@Test
	public void testDeleteAllUsers() {
		final int initialNumberOfUsers = usersPage.countNumberOfUsers();
		usersPage.createUserButton().click();
		usersPage.createNewUser("Paul", "w", "User");
		usersPage.createNewUser("Stephen", "w", "Admin");
		assertThat("Not all users are added", usersPage.countNumberOfUsers() == initialNumberOfUsers + 2);

		usersPage.closeModal();
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
		usersPage.addUsername("Felix");
		usersPage.addAndConfirmPassword("password", "password");
		usersPage.createButton().click();
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
		usersPage.createNewUser("James", "b", "User");
		usersPage.closeModal();

		body.logout();
		abcLogin("James", "b");
		usersPage.loadOrFadeWait();
		assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));
	}

	@Test
	public void testChangeOfPasswordWorksOnLogin() {
		usersPage.createUserButton().click();
		usersPage.createNewUser("James", "b", "User");
		usersPage.closeModal();

		usersPage.changePassword("James", "d");

		body.logout();
		abcLogin("James", "d");
		usersPage.loadOrFadeWait();
		assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));
	}
}
