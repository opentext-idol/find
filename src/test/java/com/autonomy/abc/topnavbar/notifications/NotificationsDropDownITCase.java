package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.NotificationsDropDown;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static org.junit.Assert.assertEquals;

public class NotificationsDropDownITCase extends ABCTestBase{
	public NotificationsDropDownITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private NotificationsDropDown notifications;
	private KeywordsPage keywordsPage;
	private CreateNewKeywordsPage createNewKeywordsPage;

	@Before
	public void setUp() {
		notifications = body.getNotifications();
	}

	@Test
	public void testCountNotifications() throws InterruptedException {
		navBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = body.getKeywordsPage();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = body.getCreateKeywordsPage();
		createNewKeywordsPage.createSynonymGroup("john juan jO", "English");
		topNavBar.notificationsDropdown();
		notifications = body.getNotifications();
		assertEquals("There should be 1 notification in the drop down", 1, notifications.countNotifications());

		navBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = body.getKeywordsPage();
		keywordsPage.deleteSynonym("john", "john");

		topNavBar.notificationsDropdown();
		notifications = body.getNotifications();
		assertEquals("There should be 2 notifications in the drop down", 2, notifications.countNotifications());

		navBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = body.getKeywordsPage();
		keywordsPage.deleteSynonym("juan", "juan");

		topNavBar.notificationsDropdown();
		notifications = body.getNotifications();
		assertEquals("There should be 3 notifications in the drop down", 3, notifications.countNotifications());
	}

	@Test
	public void testNotificationsRemainAfterPageRefresh() throws InterruptedException {
		navBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = body.getKeywordsPage();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = body.getCreateKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm("one two three four five", "English");
		topNavBar.notificationsDropdown();
		notifications = body.getNotifications();
		assertEquals("There should be 5 notifications in the drop down", 5, notifications.countNotifications());

		getDriver().navigate().refresh();
		body = new AppBody(getDriver());
		new TopNavBar(getDriver()).notificationsDropdown();
		notifications = body.getNotifications();
		assertEquals("After page refresh there should still be 5 notifications in the drop down", 5, notifications.countNotifications());
	}
}
