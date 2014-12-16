package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.page.LoginPage;
import com.autonomy.abc.selenium.page.UsersPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

public class LoginPageITCase extends ABCTestBase {

	public LoginPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private LoginPage loginPage;
	private UsersPage usersPage;

	@Before
	public void setUp() {
		topNavBar.switchPage(NavBarTabId.USERS_PAGE);
		usersPage = body.getUsersPage();
		usersPage.deleteOtherUsers();
		usersPage.createUserButton().click();
		usersPage.createNewUser("admin", "qwerty", "Admin");
		usersPage.closeModal();
		body.logout();
		loginPage = body.getLoginPage();
	}

	@Test
	public void testLoginAsNewlyCreatedAdmin() {
		loginPage.login("admin", "qwerty");
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".initial-loading-indicator")));
		assertThat("Overview page has not loaded", getDriver().getCurrentUrl().contains("overview"));

		navBar = new SideNavBar(getDriver());
		assertThat("Logged in user not displayed", navBar.getSignedInUser().equals("admin"));
	}

	@Test
	public void testLoginNotCaseSensitive() {
		loginPage.login("ADmIn", "qwerty");
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".initial-loading-indicator")));
		assertThat("Overview page has not loaded - login should not be case sensitive", getDriver().getCurrentUrl().contains("overview"));

		navBar = new SideNavBar(getDriver());
		assertThat("logged in user not displayed", navBar.getSignedInUser().equals("admin"));
	}

	@Test
	public void testPasswordCaseSensitive() {
		loginPage.login("admin", "QWERTY");
		assertThat("Navigated away from login page with invalid password", getDriver().getCurrentUrl().contains("login"));
		loginPage = body.getLoginPage();
		assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
	}

	@Test
	public void testIncorrectPassword() {
		loginPage.login("admin", "WroNG");
		assertThat("Navigated away from login page with invalid password", getDriver().getCurrentUrl().contains("login"));
		loginPage = body.getLoginPage();
		assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
	}

	@Test
	public void testInvalidUsernames() {
		for (final String invalidUserName : Arrays.asList("aadmin", " ", "admin.", "admin*", "admin/")) {
			loginPage.login(invalidUserName, "qwerty");
			assertThat("Navigated away from login page with invalid username " + invalidUserName, getDriver().getCurrentUrl().contains("login"));
			loginPage = body.getLoginPage();
			assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
		}
	}

	@Test
	public void testSQLInjection() throws IOException {
		for (final String password : loginPage.loadTextFileLineByLineIntoList("C://dev//res//sqlInj.txt")) {
			loginPage.login("admin", password);
			assertThat("Navigated away from login page with invalid password", getDriver().getCurrentUrl().contains("login"));
			loginPage = body.getLoginPage();
			assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
		}
	}
}
