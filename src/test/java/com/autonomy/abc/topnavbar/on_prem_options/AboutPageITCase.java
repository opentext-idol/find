package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.HostAndPorts;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.page.*;
import com.autonomy.abc.selenium.page.admin.AboutPage;
import com.autonomy.abc.selenium.page.admin.SettingsPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.login.LoginOnPremisePage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class AboutPageITCase extends ABCTestBase {

	public AboutPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private AboutPage aboutPage;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final Collection<TestConfig.ApplicationType> applicationTypes = Collections.singletonList(TestConfig.ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Before
	public void setUp() throws MalformedURLException {
		aboutPage = body.getAboutPage();
	}

	@Test
	public void testTableNavigation() {
		aboutPage.setTableSize("10");
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 10 of"));
		assertThat("Page 1 should be active" , aboutPage.isPageinateNumberActive(1));
		assertThat("Page 2 should not be active" , !aboutPage.isPageinateNumberActive(2));
		assertThat("Page 3 should not be active" , !aboutPage.isPageinateNumberActive(3));
		assertThat("Previous button is not disabled", aboutPage.isPreviousDisabled());

		for (int i = 0; i < 3; i++) {
			aboutPage.nextButton().click();
			assertThat("Previous button is not enabled", !aboutPage.isPreviousDisabled());
		}

		assertThat("Next button is not disabled", aboutPage.isNextDisabled());
		assertThat("Page 1 should not be active" , !aboutPage.isPageinateNumberActive(1));
		assertThat("Page 2 should not be active" , !aboutPage.isPageinateNumberActive(2));
		assertThat("Page 3 should be active" , aboutPage.isPageinateNumberActive(3));

		for (int j = 0; j < 3 ; j++) {
			aboutPage.previousButton().click();
			assertThat("Next button is not enabled", !aboutPage.isNextDisabled());
		}

		assertThat("Have not been returned to first page", aboutPage.isPageinateNumberActive(1));
		assertThat("Previous button is not disabled", aboutPage.isPreviousDisabled());
	}

	@Test
	public void testTableSize() {
		aboutPage.setTableSize("10");
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 10 of"));

		aboutPage.setTableSize("25");
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 21 of"));

		aboutPage.setTableSize("10");
		assertThat("Wrong size", aboutPage.getText().contains("Showing 1 to 10 of"));
	}

	@Test
	public void testSearchTable() {
		aboutPage.searchInSearchBox("store");
		assertThat("search has not returned correct result", aboutPage.findElement(By.cssSelector(".dataTables_wrapper tbody a")).getText().contains("store"));
	}

	public static class SettingsPageITCase extends ABCTestBase {

		public SettingsPageITCase(final TestConfig config, final String browser, final Platform platform) {
			super(config, browser, platform);
		}

		private SettingsPage settingsPage;

		private final static EnumSet<SettingsPage.Panel> SERVER_PANELS = EnumSet.of(SettingsPage.Panel.COMMUNITY, SettingsPage.Panel.CONTENT, SettingsPage.Panel.QMS, SettingsPage.Panel.QMS_AGENTSTORE, SettingsPage.Panel.STATSSERVER, SettingsPage.Panel.VIEW);

		@Parameterized.Parameters
		public static Iterable<Object[]> parameters() throws MalformedURLException {
			final Collection<TestConfig.ApplicationType> applicationTypes = Collections.singletonList(TestConfig.ApplicationType.ON_PREM);
			return parameters(applicationTypes);
		}

		@Before
		public void setUp() {
			settingsPage = body.getSettingsPage();
		}

		@Test
		public void testSaveChangesModal() {
			settingsPage.saveChangesClick();
			final ModalView saveModal = ModalView.getVisibleModalView(getDriver());
			assertThat("Correct modal not open", saveModal.getText().contains("Confirm Save"));

			settingsPage.modalCancel().click();
			settingsPage.loadOrFadeWait();

			settingsPage.saveChangesClick();
			final ModalView saveModalAgain = ModalView.getVisibleModalView(getDriver());
			assertThat("Correct modal not open", saveModalAgain.getText().contains("Confirm Save"));
			settingsPage.modalSaveChanges().click();

			settingsPage.loadOrFadeWait();
			final ModalView confirmModal = ModalView.getVisibleModalView(getDriver());
			assertThat("Correct modal not open", confirmModal.getText().contains("Success! Configuration has been saved"));

			settingsPage.modalClose();
			assertThat("Modal not closed", !saveModal.getText().contains("Confirm Save"));

		}

		@Test
		public void testRevertChangesModal() {
			settingsPage.revertChangesClick();
			final ModalView revertModal = ModalView.getVisibleModalView(getDriver());
			assertThat("Correct modal not open", revertModal.getText().contains("Revert settings"));

			settingsPage.modalCancel().click();
			settingsPage.loadOrFadeWait();

			settingsPage.revertChangesClick();
			final ModalView revertModalAgain = ModalView.getVisibleModalView(getDriver());
			assertThat("Correct modal not open", revertModalAgain.getText().contains("Revert settings"));
			settingsPage.modalOKButton().click();
		}

		@Test
		public void testAllSettingsPanelsPresent() {
			for (final SettingsPage.Panel panel : SettingsPage.Panel.values()) {
				assertTrue(settingsPage.getPanelWithName(panel.getTitle()).isDisplayed());
			}
		}

		@Test
		public void testRevertChangesPort() {
			settingsPage.saveChanges();
			final EnumMap<SettingsPage.Panel, Integer> originalPortValues = new EnumMap<>(SettingsPage.Panel.class);

			for (final SettingsPage.Panel panel : SERVER_PANELS) {
				final WebElement portBox = settingsPage.portBox(panel);
				originalPortValues.put(panel, Integer.parseInt(portBox.getAttribute("value")));
				settingsPage.changePort(1000, panel);
				assertThat(panel.getTitle() + " port should be changed to 1000", portBox.getAttribute("value").equals(Integer.toString(1000)));
			}

			settingsPage.revertChanges();
			final EnumMap<SettingsPage.Panel, Integer> finalPortValues = new EnumMap<>(SettingsPage.Panel.class);

			for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
				finalPortValues.put(settingsPanel, Integer.parseInt(settingsPage.portBox(settingsPanel).getAttribute("value")));
			}

			assertEquals(originalPortValues, finalPortValues);
		}

		@Test
		public void testRevertChangesHostname() {
			settingsPage.saveChanges();
			final EnumMap<SettingsPage.Panel, String> originalHostNames = new EnumMap<>(SettingsPage.Panel.class);

			for (final SettingsPage.Panel panel : SERVER_PANELS) {
				final WebElement hostBox = settingsPage.hostBox(panel);
				originalHostNames.put(panel, hostBox.getAttribute("value"));
				settingsPage.changeHost("richard", panel);
				assertThat(panel.getTitle() + " host should be changed to richard", hostBox.getAttribute("value").equals("richard"));
			}

			settingsPage.revertChanges();
			final EnumMap<SettingsPage.Panel, String> finalHostNames = new EnumMap<>(SettingsPage.Panel.class);

			for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
				assertThat(settingsPanel + " hostname should not equal richard", !settingsPage.hostBox(settingsPanel).getAttribute("value").equals("richard"));
				finalHostNames.put(settingsPanel, settingsPage.hostBox(settingsPanel).getAttribute("value"));
			}

			assertEquals(originalHostNames, finalHostNames);
		}

		@Test
		public void testRevertChangesProtocol() {
			settingsPage.saveChanges();
			final EnumMap<SettingsPage.Panel, String> originalProtocol = new EnumMap<>(SettingsPage.Panel.class);

			for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
				originalProtocol.put(settingsPanel, settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value"));
				settingsPage.selectProtocol("HTTPS", settingsPanel);
				assertThat(settingsPanel + " protocol should be changed to https", settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value").equals("HTTPS"));
			}
			settingsPage.revertChanges();
			final EnumMap<SettingsPage.Panel, String> finalProtocol = new EnumMap<>(SettingsPage.Panel.class);

			for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
				assertThat(settingsPanel + " hostname should not equal https", !settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value").equals("HTTPS"));
				finalProtocol.put(settingsPanel, settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value"));
			}

			assertEquals(originalProtocol, finalProtocol);
		}

		@Test
		public void testRevertToNewlySaved() {
			settingsPage.saveChanges();
			final List<SettingsPage.Panel> settingsPanels = Arrays.asList(SettingsPage.Panel.COMMUNITY, SettingsPage.Panel.QMS_AGENTSTORE);
			final List<String> originalHostNames = new ArrayList<>();

			for (final SettingsPage.Panel settingsPanel : settingsPanels) {
				originalHostNames.add(settingsPage.hostBox(settingsPanel).getAttribute("value"));
				settingsPage.changeHost("idol-admin-test-01", settingsPanel);
				assertThat(settingsPanel + " hostname should be changed to idol-admin-test-01", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("idol-admin-test-01"));
			}

			settingsPage.saveChanges();

			for (final SettingsPage.Panel settingsPanel : settingsPanels) {
				settingsPage.changeHost("andrew", settingsPanel);
				assertThat(settingsPanel + " hostname should be changed to andrew", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("andrew"));
			}

			settingsPage.revertChanges();

			for (final SettingsPage.Panel settingsPanel : settingsPanels) {
				assertThat(settingsPanel + " hostname should not be andrew", !settingsPage.hostBox(settingsPanel).getAttribute("value").equals("andrew"));
				assertThat(settingsPanel + " hostname should equal idol-admin-test-01", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("idol-admin-test-01"));
			}

			for (int i = 0; i < 2; i++) {
				settingsPage.changeHost(originalHostNames.get(i), settingsPanels.get(i));
			}
		}

		@Test
		public void testEnterBadHostAndPortNames() {
			settingsPage.saveChanges();
			settingsPage.changeHost("richard", SettingsPage.Panel.CONTENT);
			settingsPage.testConnection("Content");
		}

		@Test
		public void testBlankPortsAndHosts() {
			for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
				settingsPage.changeHost("", settingsPanel);
				settingsPage.testConnection(settingsPanel.getTitle());
				assertThat("Incorrect/No Error Message", settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("Host name must not be blank!"));

				settingsPage.changeHost("a", settingsPanel);
				settingsPage.portBox(settingsPanel).clear();
				settingsPage.testConnection(settingsPanel.getTitle());
				assertThat("Incorrect/No Error Message", settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("One or more of the required field is missing"));
			}
		}

		@After
		public void setDefaultSettings() {
			for (final SettingsPage.Panel panel : SettingsPage.Panel.values()) {
				final HostAndPorts hostAndPort = HOSTS_AND_PORTS.get(panel);

				if (hostAndPort.getPortNumber() != 0) {
					settingsPage.changePort(hostAndPort.getPortNumber(), panel);
					settingsPage.changeHost(hostAndPort.getHostName(), panel);
					settingsPage.selectProtocol("HTTP", panel);
				}
			}

			settingsPage.selectLocale("English (UK)");
			settingsPage.saveChanges();
		}

		private final static Map<SettingsPage.Panel, HostAndPorts> HOSTS_AND_PORTS;

		static {
			try (InputStream inputStream = SettingsPage.class.getResourceAsStream(System.getProperty("testConfig.location"))) {
				final ObjectMapper mapper = new ObjectMapper();
				HOSTS_AND_PORTS = mapper.readValue(inputStream, Panels.class).getServers();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@JsonDeserialize(builder = Panels.Builder.class)
		static class Panels {
			private final Map<SettingsPage.Panel, HostAndPorts> servers;

			public Panels(final Builder builder) {
				this.servers = builder.servers;
			}

			public Map<SettingsPage.Panel, HostAndPorts> getServers() {
				return servers;
			}

			@JsonPOJOBuilder(withPrefix = "set")
			static class Builder {
				private Map<SettingsPage.Panel, HostAndPorts> servers;

				public Builder setServers(final EnumMap<SettingsPage.Panel, HostAndPorts> servers) {
					this.servers = servers;
					return this;
				}

				public Panels build() {
					return new Panels(this);
				}
			}
		}
	}

	public static class UsersPageITCase extends ABCTestBase {

		private static final String STUPIDLY_LONG_USERNAME = "StupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserNameStupidlyLongUserName";

		public UsersPageITCase(final TestConfig config, final String browser, final Platform platform) {
			super(config, browser, platform);
		}

		private UsersPage usersPage;

		@Parameterized.Parameters
		public static Iterable<Object[]> parameters() throws MalformedURLException {
			final Collection<TestConfig.ApplicationType> applicationTypes = Collections.singletonList(TestConfig.ApplicationType.ON_PREM);
			return parameters(applicationTypes);
		}

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
			assertThat("Delete button not disabled", usersPage.isAttributePresent((usersPage.getUserRow(usersPage.getSignedInUserName()).findElement(By.cssSelector("button"))), "disabled"));
		}

		@Test
		public void testDeleteAllUsers() {
			final int initialNumberOfUsers = usersPage.countNumberOfUsers();
			usersPage.createUserButton().click();
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
			abcOnPremiseLogin("James", "b");
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
			abcOnPremiseLogin("James", "d");
			usersPage.loadOrFadeWait();
			assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));
		}

		@Test
		public void testCreateUserPermissionNoneAndTestLogin() {
			usersPage.createUserButton().click();
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

			body.logout();
			LoginOnPremisePage loginPage = body.getLoginOnPremisePage();
			loginPage.login("Norman", "n");
			loginPage = body.getLoginOnPremisePage();
			assertThat("Wrong/no error message displayed", loginPage.getText().contains("Please check your username and password."));
			assertThat("URL wrong", getDriver().getCurrentUrl().contains("login/index"));
		}

		@Test
		public void testAnyUserCanNotAccessConfigPage() {
			String baseUrl = config.getWebappUrl();
			baseUrl = baseUrl.substring(0, baseUrl.length() - 2);
			getDriver().get(baseUrl + "config");
			body.loadOrFadeWait();
			assertFalse(getDriver().getCurrentUrl().contains("config"));
			Assert.assertTrue(getDriver().getCurrentUrl().contains("overview"));
		}

		@Test
		public void testUserCannotAccessUsersPageOrSettingsPage() {
			usersPage.createUserButton().click();
			usersPage.createNewUser("James", "b", "User");
			usersPage.closeModal();

			body.logout();
			abcOnPremiseLogin("James", "b");
			usersPage.loadOrFadeWait();
			assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));

			getDriver().get(config.getWebappUrl() + "settings");
			body.loadOrFadeWait();
			assertFalse(getDriver().getCurrentUrl().contains("settings"));
			Assert.assertTrue(getDriver().getCurrentUrl().contains("overview"));

			getDriver().get(config.getWebappUrl() + "users");
			body.loadOrFadeWait();
			assertFalse(getDriver().getCurrentUrl().contains("users"));
			Assert.assertTrue(getDriver().getCurrentUrl().contains("overview"));
		}

		@Test
		public void testXmlHttpRequestToUserConfigBlockedForInadequatePermissions() throws UnhandledAlertException {
			usersPage.createUserButton().click();
			usersPage.createNewUser("James", "b", "User");
			usersPage.closeModal();
			body.logout();

			abcOnPremiseLogin("James", "b");
			usersPage.loadOrFadeWait();
			assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));

			final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
			executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function(xhr) {$('body').attr('data-status', xhr.status);});");
			usersPage.loadOrFadeWait();
			Assert.assertTrue(getDriver().findElement(By.cssSelector("body")).getAttribute("data-status").contains("403"));

			body = new AppBody(getDriver());
			body.logout();

			abcOnPremiseLogin("richard", "q");
			usersPage.loadOrFadeWait();
			assertThat("Login not successful", getDriver().getCurrentUrl().endsWith("overview"));

			executor.executeScript("$.get('/searchoptimizer/api/admin/config/users').error(function() {alert(\"error\");});");
			usersPage.loadOrFadeWait();
			assertFalse(usersPage.isAlertPresent());
		}
	}
}
