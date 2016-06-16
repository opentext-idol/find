package com.autonomy.abc.users;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.selenium.auth.IdolIsoAccount;
import com.autonomy.abc.selenium.auth.IdolIsoNewUser;
import com.autonomy.abc.selenium.users.IdolIsoLoginPage;
import com.autonomy.abc.selenium.users.UserService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasAttribute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class LoginPageOnPremiseITCase extends IdolIsoTestBase {
	private IdolIsoLoginPage loginPage;

	public LoginPageOnPremiseITCase(final TestConfig config) {
		super(config);
	}

	@Before
	public void setUp() {
        final UserService userService = getApplication().userService();
		userService.deleteOtherUsers();
		userService.createNewUser(new IdolIsoNewUser("admin", "qwerty"), Role.ADMIN);
		getApplication().loginService().logout();
		loginPage = getElementFactory().getLoginPage();
	}

	@Test
	public void testLoginAsNewlyCreatedAdmin() {
        loginPage.loginWith(new IdolIsoAccount("admin", "qwerty"));
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".initial-loading-indicator")));
		assertThat("Overview page has not loaded", getWindow(), urlContains("overview"));
	}

	@Test
	public void testLoginNotCaseSensitive() {
        loginPage.loginWith(new IdolIsoAccount("ADmIn", "qwerty"));
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".initial-loading-indicator")));
		assertThat("Overview page has not loaded - login should not be case sensitive", getWindow(), urlContains("overview"));
	}

	@Test
	public void testPasswordCaseSensitive() {
		loginPage.loginWith(new IdolIsoAccount("admin", "QWERTY"));
		assertThat("Navigated away from login page with invalid password", getWindow(), urlContains("login"));
		loginPage = getElementFactory().getLoginPage();
		assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
	}

	@Test
	public void testIncorrectPassword() {
		loginPage.loginWith(new IdolIsoAccount("admin", "WroNG"));
		assertThat("Navigated away from login page with invalid password", getWindow(), urlContains("login"));
		loginPage = getElementFactory().getLoginPage();
		assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
	}

	@Test
	public void testInvalidUsernames() {
		for (final String invalidUserName : Arrays.asList("aadmin", " ", "admin.", "admin*", "admin/")) {
			loginPage.loginWith(new IdolIsoAccount(invalidUserName, "qwerty"));
			assertThat("Navigated away from login page with invalid username " + invalidUserName, getWindow(), urlContains("login"));
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

    @Ignore //Do not have the correct txt file
	@Test
	public void testSQLInjection() throws IOException {
		for (final String password : loadTextFileLineByLineIntoList("C://dev//res//sqlInj.txt")) {
			loginPage.loginWith(new IdolIsoAccount("admin", password));
			assertThat("Navigated away from login page with invalid password", getWindow(), urlContains("login"));
			loginPage = getElementFactory().getLoginPage();
			assertThat("Correct error message not showing", loginPage.getText().contains("Please check your username and password"));
		}
	}

	@Test
	public void testLogoutNoAccessViaUrl() {
		getDriver().get(getAppUrl() + "overview");
		Waits.loadOrFadeWait();
		assertThat(getWindow(), url(not(containsString("overview"))));
		assertThat(getWindow(), urlContains("login"));

		getDriver().get(getAppUrl() + "keywords");
		Waits.loadOrFadeWait();
		assertThat(getWindow(), url(not(containsString("keywords"))));
		assertThat(getWindow(), urlContains("login"));
	}

	@Test
	public void testDefaultLoginDisabled() {
		getDriver().get(getAppUrl().replace("p/promotions", "login?defaultLogin=admin"));
		Waits.loadOrFadeWait();
		loginPage = getElementFactory().getLoginPage();
		verifyThat(loginPage.usernameInput(), not(hasAttribute("readonly")));
		assertThat(getWindow(), url(not(containsString("defaultLogin"))));
	}
}
