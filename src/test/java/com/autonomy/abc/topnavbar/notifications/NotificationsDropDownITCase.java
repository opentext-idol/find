package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.NotificationsDropDown;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

public class NotificationsDropDownITCase extends ABCTestBase{
	public NotificationsDropDownITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
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
		createNewKeywordsPage.createSynonymGroup("John Juan JO", "English");
		body.getNotifications();
		final int number = notifications.countNotifications();
		System.out.println(number);
	}
}
