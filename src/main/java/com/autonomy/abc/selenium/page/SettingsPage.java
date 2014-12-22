package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SettingsPage extends AppElement implements AppPage {

	public SettingsPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage() { getDriver().get("settings"); }

	public void saveChangesClick() {
		findElement(By.xpath(".//*[contains(text(), 'Save Changes')]")).click();
		loadOrFadeWait();
	}

	public void saveChanges() {
		loadOrFadeWait();
		saveChangesClick();
		modalSaveChanges().click();
		loadOrFadeWait();
		modalClose();
	}

	public void revertChanges() {
		revertChangesClick();
		loadOrFadeWait();
		modalOKButton().click();
		loadOrFadeWait();
	}

	public void revertChangesClick() {
		findElement(By.xpath(".//*[contains(text(), 'Revert Changes')]")).click();
		loadOrFadeWait();
	}

	public WebElement modalSaveChanges() {
		final ModalView saveModal = ModalView.getVisibleModalView(getDriver());
		return saveModal.findElement(By.xpath(".//*[contains(text(), 'Save Changes')]"));
	}

	public WebElement modalCancel() {
		final ModalView modal = ModalView.getVisibleModalView(getDriver());
		return modal.findElement(By.xpath(".//*[contains(text(), 'Cancel')]"));
	}

	public WebElement modalOKButton() {
		final ModalView modal = ModalView.getVisibleModalView(getDriver());
		return modal.findElement(By.xpath(".//*[contains(text(), 'OK')]"));
	}

	public WebElement getPanelWithName(final String panelName) {
		return findElement(By.xpath(".//h3[contains(text(), '" + panelName + "')]/../.."));
	}

	public void changePort(final int portNumber, final String panelName) {
		loadOrFadeWait();
		portBox(panelName).clear();
		loadOrFadeWait();
		portBox(panelName).sendKeys(Integer.toString(portNumber));
	}

	public WebElement portBox(final String panelName) {
		return getPanelWithName(panelName).findElement(By.cssSelector("[placeholder='port']"));
	}

	public void modalClose() {
		loadOrFadeWait();
		final ModalView modal = ModalView.getVisibleModalView(getDriver());
		modal.findElement(By.xpath(".//*[contains(text(), 'Close')]")).click();
		loadOrFadeWait();
	}

	public WebElement hostBox(final String panelName) {
		return getPanelWithName(panelName).findElement(By.cssSelector("[placeholder='hostname']"));
	}

	public void changeHost(final String hostname, final String panelName) {
		loadOrFadeWait();
		hostBox(panelName).clear();
		loadOrFadeWait();
		hostBox(panelName).sendKeys(hostname);
	}

	public WebElement protocolBox(final String panelName) {
		return getPanelWithName(panelName).findElement(By.cssSelector("[name='protocol']"));
	}

	public void selectProtocol(final String protocol, final String panelName) {
		protocolBox(panelName).findElement(By.cssSelector("[value='" + protocol + "']")).click();
	}

	public WebElement defaultLocale() {
		return getPanelWithName("Locale").findElement(By.name("locale"));
	}

	public void selectLocale(final String locale) {
		defaultLocale().findElement(By.xpath(".//*[contains(text(), '" + locale + "')]")).click();
	}

	public void testConnection(final String panelName) {
		getPanelWithName(panelName).findElement(By.xpath(".//*[contains(text(), 'Test Connection')]")).click();
	}

	public void returnToDefaults() {
		for (final Panel panel : Panel.values()) {
			if (panel.defaultHost != null) {
				changePort(panel.defaultPort, panel.title);
				changeHost(panel.defaultHost, panel.title);
				selectProtocol("HTTP", panel.title);
			}
		}

		selectLocale("English (UK)");
		saveChanges();
	}

	public enum Panel {
		COMMUNITY("Community", "localhost", 9030),
		CONTENT("Content", "localhost", 9000),
		QMS("QMS", "localhost", 16000),
		LOCALE("Locale", null, null),
		QMS_AGENTSTORE("QMS Agentstore", "localhost", 9050),
		STATSSERVER("IDOL StatsServer", "localhost", 19870);

		private final String title;
		private final String defaultHost;
		private final Integer defaultPort;

		Panel(final String title, final String defaultHost, final Integer defaultPort) {
			this.title = title;
			this.defaultHost = defaultHost;
			this.defaultPort = defaultPort;
		}

		public String getTitle() {
			return title;
		}
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<SettingsPage> {

		public Placeholder(final AppBody body, final SideNavBar mainTabBar, final TopNavBar topNavBar) {
			super(body, mainTabBar, topNavBar, "settings", NavBarTabId.SETTINGS, false);
		}

		@Override
		protected SettingsPage convertToActualType(final WebElement element) {
			return new SettingsPage(topNavBar, element);
		}

	}

}


