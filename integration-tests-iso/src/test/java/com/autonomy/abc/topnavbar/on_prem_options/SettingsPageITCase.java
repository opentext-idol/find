package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.config.IdolHostAndPorts;
import com.autonomy.abc.config.DualConfigLocator;
import com.autonomy.abc.selenium.iso.IsoSettingsPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openqa.selenium.lift.Matchers.displayed;


public class SettingsPageITCase extends IdolIsoTestBase {
	private final static Map<IsoSettingsPage.Panel, IdolHostAndPorts> HOSTS_AND_PORTS;
	private final static EnumSet<IsoSettingsPage.Panel> SERVER_PANELS = EnumSet.of(IsoSettingsPage.Panel.COMMUNITY, IsoSettingsPage.Panel.CONTENT, IsoSettingsPage.Panel.QMS, IsoSettingsPage.Panel.QMS_AGENTSTORE, IsoSettingsPage.Panel.STATSSERVER, IsoSettingsPage.Panel.VIEW);

	private IsoSettingsPage settingsPage;

	static {
		try {
			JsonNode node = new DualConfigLocator().getJsonNode().path("servers");
			HOSTS_AND_PORTS = new ObjectMapper().convertValue(node, new TypeReference<Map<IsoSettingsPage.Panel, IdolHostAndPorts>>() {});
			System.out.println(HOSTS_AND_PORTS);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public SettingsPageITCase(final TestConfig config) {
		super(config);
	}

	@Before
	public void setUp() throws InterruptedException {
		settingsPage = getApplication().switchTo(IsoSettingsPage.class);
	}

	@Test
	public void testSaveChangesModal() {
		settingsPage.saveChangesClick();
		final ModalView saveModal = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", saveModal.getText().contains("Confirm Save"));

		settingsPage.modalCancel().click();
		Waits.loadOrFadeWait();

		settingsPage.saveChangesClick();
		final ModalView saveModalAgain = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", saveModalAgain.getText().contains("Confirm Save"));
		settingsPage.modalSaveChanges().click();

		Waits.loadOrFadeWait();
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
		Waits.loadOrFadeWait();

		settingsPage.revertChangesClick();
		final ModalView revertModalAgain = ModalView.getVisibleModalView(getDriver());
		assertThat("Correct modal not open", revertModalAgain.getText().contains("Revert settings"));
		settingsPage.modalOKButton().click();
	}

	@Test
	public void testAllSettingsPanelsPresent() {
		for (final IsoSettingsPage.Panel panel : IsoSettingsPage.Panel.values()) {
			if (panel.equals(IsoSettingsPage.Panel.LOCALE)) continue;

			assertThat(settingsPage.getPanelWithName(panel.getTitle()), displayed());
		}
	}

	@Test
	public void testRevertChangesPort() {
		settingsPage.saveChanges();
		final EnumMap<IsoSettingsPage.Panel, Integer> originalPortValues = new EnumMap<>(IsoSettingsPage.Panel.class);

		for (final IsoSettingsPage.Panel panel : SERVER_PANELS) {
			final WebElement portBox = settingsPage.portBox(panel);
			originalPortValues.put(panel, Integer.parseInt(portBox.getAttribute("value")));
			settingsPage.changePort(1000, panel);
			assertThat(panel.getTitle() + " port should be changed to 1000", portBox.getAttribute("value").equals(Integer.toString(1000)));
		}

		settingsPage.revertChanges();
		final EnumMap<IsoSettingsPage.Panel, Integer> finalPortValues = new EnumMap<>(IsoSettingsPage.Panel.class);

		for (final IsoSettingsPage.Panel settingsPanel : SERVER_PANELS) {
			finalPortValues.put(settingsPanel, Integer.parseInt(settingsPage.portBox(settingsPanel).getAttribute("value")));
		}

		assertThat(originalPortValues, is(finalPortValues));
	}

	@Test
	public void testRevertChangesHostname() {
		settingsPage.saveChanges();
		final EnumMap<IsoSettingsPage.Panel, String> originalHostNames = new EnumMap<>(IsoSettingsPage.Panel.class);

		for (final IsoSettingsPage.Panel panel : SERVER_PANELS) {
			final WebElement hostBox = settingsPage.hostBox(panel);
			originalHostNames.put(panel, hostBox.getAttribute("value"));
			settingsPage.changeHost("richard", panel);
			assertThat(panel.getTitle() + " host should be changed to richard", hostBox.getAttribute("value").equals("richard"));
		}

		settingsPage.revertChanges();
		final EnumMap<IsoSettingsPage.Panel, String> finalHostNames = new EnumMap<>(IsoSettingsPage.Panel.class);

		for (final IsoSettingsPage.Panel settingsPanel : SERVER_PANELS) {
			assertThat(settingsPanel + " hostname should not equal richard", !settingsPage.hostBox(settingsPanel).getAttribute("value").equals("richard"));
			finalHostNames.put(settingsPanel, settingsPage.hostBox(settingsPanel).getAttribute("value"));
		}

		assertThat(originalHostNames, is(finalHostNames));
	}

	@Test
	public void testRevertChangesProtocol() {
		settingsPage.saveChanges();
		final EnumMap<IsoSettingsPage.Panel, String> originalProtocol = new EnumMap<>(IsoSettingsPage.Panel.class);

		for (final IsoSettingsPage.Panel settingsPanel : SERVER_PANELS) {
			originalProtocol.put(settingsPanel, settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value"));
			settingsPage.selectProtocol("HTTPS", settingsPanel);
			assertThat(settingsPanel + " protocol should be changed to https", settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value").equals("HTTPS"));
		}
		settingsPage.revertChanges();
		final EnumMap<IsoSettingsPage.Panel, String> finalProtocol = new EnumMap<>(IsoSettingsPage.Panel.class);

		for (final IsoSettingsPage.Panel settingsPanel : SERVER_PANELS) {
			assertThat(settingsPanel + " hostname should not equal https", !settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value").equals("HTTPS"));
			finalProtocol.put(settingsPanel, settingsPage.protocolBox(settingsPanel.getTitle()).getAttribute("value"));
		}

		assertThat(originalProtocol, is(finalProtocol));
	}

	@Test
	public void testRevertToNewlySaved() {
		settingsPage.saveChanges();
		final List<IsoSettingsPage.Panel> settingsPanels = Arrays.asList(IsoSettingsPage.Panel.COMMUNITY, IsoSettingsPage.Panel.QMS_AGENTSTORE);
		final List<String> originalHostNames = new ArrayList<>();

		for (final IsoSettingsPage.Panel settingsPanel : settingsPanels) {
			originalHostNames.add(settingsPage.hostBox(settingsPanel).getAttribute("value"));
			settingsPage.changeHost("idol-admin-test-01", settingsPanel);
			assertThat(settingsPanel + " hostname should be changed to idol-admin-test-01", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("idol-admin-test-01"));
		}

		settingsPage.saveChanges();

		for (final IsoSettingsPage.Panel settingsPanel : settingsPanels) {
			settingsPage.changeHost("andrew", settingsPanel);
			assertThat(settingsPanel + " hostname should be changed to andrew", settingsPage.hostBox(settingsPanel).getAttribute("value").equals("andrew"));
		}

		settingsPage.revertChanges();

		for (final IsoSettingsPage.Panel settingsPanel : settingsPanels) {
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
		settingsPage.changeHost("richard", IsoSettingsPage.Panel.CONTENT);
		settingsPage.testConnection("Content");
	}

	@Test
	public void testBlankPortsAndHosts() {
		for (final IsoSettingsPage.Panel settingsPanel : SERVER_PANELS) {
			settingsPage.changeHost("", settingsPanel);
			settingsPage.testConnection(settingsPanel.getTitle());
			assertThat("Incorrect/No Error Message", settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("Host name must not be blank!"));

			settingsPage.changeHost("a", settingsPanel);
			settingsPage.portBox(settingsPanel).clear();
			settingsPage.testConnection(settingsPanel.getTitle());
			assertThat("Incorrect/No Error Message in panel " + settingsPanel.getTitle(), settingsPage.getPanelWithName(settingsPanel.getTitle()).getText().contains("Port must not be blank, and inside the range 1-65535"));
		}
	}

	@After
	public void setDefaultSettings() {
		for (final IsoSettingsPage.Panel panel : IsoSettingsPage.Panel.values()) {
			final IdolHostAndPorts hostAndPort = HOSTS_AND_PORTS.get(panel);

			if (hostAndPort.getPortNumber() != 0) {
				settingsPage.changePort(hostAndPort.getPortNumber(), panel);
				settingsPage.changeHost(hostAndPort.getHostName(), panel);
				settingsPage.selectProtocol("HTTP", panel);
			}
		}

		settingsPage.saveChanges();
	}

}
