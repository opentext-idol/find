package com.autonomy.abc.topnavbar.login;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.login.OPAccount;
import com.autonomy.abc.selenium.page.login.OPLoginPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasAttribute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginPageOnPremiseITCase extends ABCTestBase {

	public LoginPageOnPremiseITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private OPLoginPage loginPage;
	private UsersPage usersPage;

	@Before
	public void setUp() {
		topNavBar.switchPage(NavBarTabId.USERS_PAGE);
		usersPage = (UsersPage) getElementFactory().getUsersPage();
		usersPage.deleteOtherUsers();
		usersPage.createUserButton().click();
		assertTrue("Create user modal has not opened", usersPage.isModalShowing());
		usersPage.createNewUser("admin", "qwerty", "Admin");
		usersPage.closeModal();
		body.logout();
		loginPage = getElementFactory().getLoginPage();
	}

	@Test
	public void testLoginAsNewlyCreatedAdmin() {
        loginPage.loginWith(new OPAccount("admin", "qwerty"));
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".initial-loading-indicator")));
		assertThat("Overview page has not loaded", getDriver().getCurrentUrl().contains("overview"));
	}

	@Test
	public void testLoginNotCaseSensitive() {
        loginPage.loginWith(new OPAccount("ADmIn", "qwerty"));
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".initial-loading-indicator")));
		assertThat("Overview page has not loaded - login should not be case sensitive", getDriver().getCurrentUrl().contains("overview"));
	}

	@Test
	public void testPasswordCaseSensitive() {
		loginPage.loginWith(new OPAccount("admin", "QWERTY"));
		assertThat("Navigated away from login page with invalid password", getDriver().getCurrentUrl().contains("login"));
		loginPage = getElementFactory().getLoginPage();
		assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
	}

	@Test
	public void testIncorrectPassword() {
		loginPage.loginWith(new OPAccount("admin", "WroNG"));
		assertThat("Navigated away from login page with invalid password", getDriver().getCurrentUrl().contains("login"));
		loginPage = getElementFactory().getLoginPage();
		assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
	}

	@Test
	public void testInvalidUsernames() {
		for (final String invalidUserName : Arrays.asList("aadmin", " ", "admin.", "admin*", "admin/")) {
			loginPage.loginWith(new OPAccount(invalidUserName, "qwerty"));
			assertThat("Navigated away from login page with invalid username " + invalidUserName, getDriver().getCurrentUrl().contains("login"));
			loginPage = getElementFactory().getLoginPage();
			assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
		}
	}

    private List<String> loadTextFileLineByLineIntoList(final String filePath) throws IOException {
        final FileInputStream fis = new FileInputStream(filePath);

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            final List<String> fiveWords = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                fiveWords.add(line);
            }

            return fiveWords;
        }
    }

	@Test
	public void testSQLInjection() throws IOException {
		for (final String password : loadTextFileLineByLineIntoList("C://dev//res//sqlInj.txt")) {
			loginPage.loginWith(new OPAccount("admin", password));
			assertThat("Navigated away from login page with invalid password", getDriver().getCurrentUrl().contains("login"));
			loginPage = getElementFactory().getLoginPage();
			assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
		}
	}

	@Test
	public void testLogoutNoAccessViaUrl() {
		getDriver().get(config.getWebappUrl() + "overview");
		body.loadOrFadeWait();
		assertFalse(getDriver().getCurrentUrl().contains("overview"));
		assertTrue(getDriver().getCurrentUrl().contains("login"));

		getDriver().get(config.getWebappUrl() + "keywords");
		body.loadOrFadeWait();
		assertFalse(getDriver().getCurrentUrl().contains("keywords"));
		assertTrue(getDriver().getCurrentUrl().contains("login"));
	}

	@Test
	public void testDefaultLoginDisabled() {
		getDriver().get(config.getWebappUrl().substring(0, config.getWebappUrl().length() - 2) + "login?defaultLogin=admin");
		body.loadOrFadeWait();
		loginPage = getElementFactory().getLoginPage();
		verifyThat(loginPage.usernameInput(), not(hasAttribute("readonly")));
		assertFalse(getDriver().getCurrentUrl().contains("defaultLogin"));
	}
}
