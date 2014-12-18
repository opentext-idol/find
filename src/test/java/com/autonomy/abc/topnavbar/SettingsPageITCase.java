package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.page.SettingsPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SettingsPageITCase extends ABCTestBase{

	public SettingsPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private SettingsPage settingsPage;

	private final static EnumSet<SettingsPage.Panel> SERVER_PANELS = EnumSet.of(SettingsPage.Panel.COMMUNITY, SettingsPage.Panel.CONTENT, SettingsPage.Panel.QMS, SettingsPage.Panel.QMS_AGENTSTORE, SettingsPage.Panel.STATSSERVER);

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
			final WebElement portBox = settingsPage.portBox(panel.getTitle());
			originalPortValues.put(panel, Integer.parseInt(portBox.getAttribute("value")));
			settingsPage.changePort(1000, panel.getTitle());
			assertThat(panel.getTitle() + " port should be changed to 1000", portBox.getAttribute("value").equals(Integer.toString(1000)));
		}

		settingsPage.revertChanges();
		final EnumMap<SettingsPage.Panel, Integer> finalPortValues = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			finalPortValues.put(settingsPanel, Integer.parseInt(settingsPage.portBox(settingsPanel.getTitle()).getAttribute("value")));
		}

		assertEquals(originalPortValues, finalPortValues);
	}

	@Test
	public void testRevertChangesHostname() {
		settingsPage.saveChanges();
		final EnumMap<SettingsPage.Panel, String> originalHostNames = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel panel : SERVER_PANELS) {
			final WebElement hostBox = settingsPage.hostBox(panel.getTitle());
			originalHostNames.put(panel, hostBox.getAttribute("value"));
			settingsPage.changeHost("richard", panel.getTitle());
			assertThat(panel.getTitle() + " host should be changed to richard", hostBox.getAttribute("value").equals("richard"));
		}

		settingsPage.revertChanges();
		final EnumMap<SettingsPage.Panel, String> finalHostNames = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			assertThat(settingsPanel + " hostname should not equal richard", !settingsPage.hostBox(settingsPanel.getTitle()).getAttribute("value").equals("richard"));
			finalHostNames.put(settingsPanel, settingsPage.hostBox(settingsPanel.getTitle()).getAttribute("value"));
		}

		assertEquals(originalHostNames, finalHostNames);
	}

	@Test
	public void testRevertChangesProtocol() {
		settingsPage.saveChanges();
		final EnumMap<SettingsPage.Panel, String> originalProtocol = new EnumMap<>(SettingsPage.Panel.class);

		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			originalProtocol.put(settingsPanel, settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value"));
			settingsPage.selectProtocol("HTTPS", settingsPanel.getTitle());
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
		settingsPage.saveChanges();
		final List<String> settingsPanels = Arrays.asList(SettingsPage.Panel.COMMUNITY.getTitle(), SettingsPage.Panel.QMS_AGENTSTORE.getTitle());
		final List<String> originalHostNames = new ArrayList<>();

		for (final String settingsPanel : settingsPanels) {
			originalHostNames.add(settingsPage.hostBox(settingsPanel).getAttribute("value"));
			settingsPage.changeHost("idol-admin-test-01", settingsPanel);
			assertThat(settingsPanel + " hostname should be changed to idol-admin-test-01", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("idol-admin-test-01"));
		}

		settingsPage.saveChanges();

		for (final String settingsPanel : settingsPanels) {
			settingsPage.changeHost("andrew", settingsPanel);
			assertThat(settingsPanel + " hostname should be changed to andrew", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("andrew"));
		}

		settingsPage.revertChanges();

		for (final String settingsPanel : settingsPanels) {
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
		settingsPage.changeHost("richard", SettingsPage.Panel.CONTENT.getTitle());
		settingsPage.testConnection("Content");
	}

	@Test
	public void testBlankPortsAndHosts() {
		for (final SettingsPage.Panel settingsPanel : SERVER_PANELS) {
			settingsPage.changeHost("", settingsPanel.getTitle());
			settingsPage.testConnection(settingsPanel.getTitle());
			assertThat("", settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("Host name must not be blank!"));

			settingsPage.changeHost("a", settingsPanel.getTitle());
			settingsPage.portBox(settingsPanel.getTitle()).clear();
			settingsPage.testConnection(settingsPanel.getTitle());
			assertThat("", settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("One or more of the required field is missing"));
		}
	}

	@After
	public void setDefaultSettings() {
		settingsPage.returnToDefaults();
	}
}
