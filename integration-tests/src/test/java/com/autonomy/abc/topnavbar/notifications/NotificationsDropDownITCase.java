package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

public class NotificationsDropDownITCase extends NotificationsDropDownTestBase {
	public NotificationsDropDownITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	@Test
	public void testCountNotifications() throws InterruptedException {
		sideNavBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createSynonymGroup("john juan jO", "English");
		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("There should be 1 notification in the drop down", notifications.countNotifications(), is(1));

		sideNavBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.deleteSynonym("john", "john");

		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("There should be 2 notifications in the drop down", notifications.countNotifications(), is(2));

		sideNavBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.deleteSynonym("juan", "juan");

		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("There should be 3 notifications in the drop down", notifications.countNotifications(), is(3));
	}

	@Test
	public void testNotificationsRemainAfterPageRefresh() throws InterruptedException {
		sideNavBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm("one two three four five", "English");
		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("There should be 5 notifications in the drop down", notifications.countNotifications(), is(5));

		getDriver().navigate().refresh();
		newBody();
		body.getTopNavBar().notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("After page refresh there should still be 5 notifications in the drop down", notifications.countNotifications(), is(5));
	}

	//Fails because of CSA-1542
	@Test
	public void testNotificationsOverTwoWindows() throws InterruptedException {
		sideNavBar.switchPage(NavBarTabId.KEYWORDS);

		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat(notifications.countNotifications(), is(0));

		keywordsPage = getElementFactory().getKeywordsPage();
		List<String> browserHandles = keywordsPage.createAndListWindowHandles();

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().navigate().to(getConfig().getWebappUrl());
		AppBody bodyWindowTwo = getBody();
		TopNavBar topNavBarWindowTwo = bodyWindowTwo.getTopNavBar();
		SideNavBar sideNavBarWindowTwo = bodyWindowTwo.getSideNavBar();
		getDriver().manage().window().maximize();

		sideNavBarWindowTwo.switchPage(NavBarTabId.KEYWORDS);
		topNavBarWindowTwo.notificationsDropdown();
		NotificationsDropDown notificationsDropDownWindowTwo = topNavBarWindowTwo.getNotifications();
		assertThat(notificationsDropDownWindowTwo.countNotifications(), is(0));

		try {
			getDriver().switchTo().window(browserHandles.get(0));
			keywordsPage.createNewKeywordsButton().click();
			getElementFactory().getCreateNewKeywordsPage().createSynonymGroup("Animal Beast", "English");
			getElementFactory().getSearchPage();
			sideNavBar.switchPage(NavBarTabId.KEYWORDS);
			keywordsPage = getElementFactory().getKeywordsPage();
			new WebDriverWait(getDriver(), 5).until(GritterNotice.notificationAppears());

			topNavBar.notificationsDropdown();
			notifications = topNavBar.getNotifications();
			assertThat(notifications.countNotifications(), is(1));
			String windowOneNotificationText = notifications.notificationNumber(1).getText();

			getDriver().switchTo().window(browserHandles.get(1));
			assertThat(notificationsDropDownWindowTwo.countNotifications(), is(1));
			assertThat(notificationsDropDownWindowTwo.notificationNumber(1).getText(), is(windowOneNotificationText));
			topNavBarWindowTwo.notificationsDropdown();
			KeywordsPage keywordsPageWindowTwo = getElementFactory().getKeywordsPage();
			keywordsPageWindowTwo.deleteSynonym("Animal", "Animal");
			topNavBarWindowTwo.notificationsDropdown();
			assertThat(notificationsDropDownWindowTwo.countNotifications(), is(2));
			List<String> notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

			getDriver().switchTo().window(browserHandles.get(0));
			assertThat(notifications.countNotifications(), is(2));
			assertThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));

			if (getConfig().getType().equals(ApplicationType.HOSTED)) {
				sideNavBar.switchPage(NavBarTabId.ANALYTICS);
				newBody();
				((HSOElementFactory) getElementFactory()).getAnalyticsPage();
				topNavBar.notificationsDropdown();
				notifications = topNavBar.getNotifications();
				assertThat(notifications.countNotifications(), is(2));
				assertThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));
			}

			getDriver().switchTo().window(browserHandles.get(1));

			PromotionService promotionService = getApplication().createPromotionService(getElementFactory());
			promotionService.setUpPromotion(new SpotlightPromotion("wheels"), new SearchActionFactory(getApplication(), getElementFactory()).makeSearch("cars"), 3);

			new WebDriverWait(getDriver(), 5).until(GritterNotice.notificationAppears());
			topNavBarWindowTwo.notificationsDropdown();
			notificationsDropDownWindowTwo = topNavBarWindowTwo.getNotifications();

			assertThat(notificationsDropDownWindowTwo.countNotifications(), is(3));
			assertThat(notificationsDropDownWindowTwo.notificationNumber(1).getText(), containsString("promotion"));

			notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

			getDriver().switchTo().window(browserHandles.get(0));

			notifications = topNavBar.getNotifications();
			assertThat(notifications.countNotifications(), is(3));
			assertThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));

			int notificationsCount = 3;
			for(int i = 0; i < 6; i += 2) {
				getDriver().switchTo().window(browserHandles.get(1));
				body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
				getElementFactory().getKeywordsPage().createNewKeywordsButton().click();
				getElementFactory().getCreateNewKeywordsPage().createSynonymGroup(i + " " + (i + 1), "English");
				getElementFactory().getSearchPage();
				new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationAppears());
				bodyWindowTwo.getTopNavBar().notificationsDropdown();
				assertThat(notificationsDropDownWindowTwo.countNotifications(), is(Math.min(++notificationsCount, 5)));
				notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

				getDriver().switchTo().window(browserHandles.get(0));
				assertThat(notifications.countNotifications(), is(Math.min(notificationsCount, 5)));
				assertThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));
			}
		} finally {
			getDriver().switchTo().window(browserHandles.get(1));

			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
			getElementFactory().getKeywordsPage().deleteKeywords();
			body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
			getElementFactory().getPromotionsPage().deleteAllPromotions();
		}
	}

	@Test
	public void testSynonymNotifications() throws InterruptedException {
		String synonymOne = "Brock".toLowerCase();
		String synonymTwo = "Lesnar".toLowerCase();
		String synonymNotificationText = "Created a new synonym group containing: "+synonymOne+", "+synonymTwo;

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		getElementFactory().getKeywordsPage().createNewKeywordsButton().click();
		getElementFactory().getCreateNewKeywordsPage().createSynonymGroup(synonymOne + " " + synonymTwo, "English");
		try {
			getElementFactory().getSearchPage();
			checkForNotification(synonymNotificationText);
		} finally {
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
			getElementFactory().getKeywordsPage().deleteKeywords();
		}
	}

	@Test
	public void testBlacklistNotifications() throws InterruptedException {
		String blacklistOne = "Rollins".toLowerCase();
		String blacklistTwo = "Seth".toLowerCase();
		String blacklistNotificationText = "Added \"placeholder\" to the blacklist";

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.deleteKeywords();
		keywordsPage.createNewKeywordsButton().click();
		getElementFactory().getCreateNewKeywordsPage().createBlacklistedTerm(blacklistOne + " " + blacklistTwo, "English");
		try {
			getElementFactory().getKeywordsPage();
			new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationContaining(blacklistNotificationText.replace("placeholder", blacklistOne)));
			body.getTopNavBar().notificationsDropdown();
			notifications = body.getTopNavBar().getNotifications();
			assertThat(notifications.notificationNumber(1).getText(), anyOf(is(blacklistNotificationText.replace("placeholder", blacklistOne)), is(blacklistNotificationText.replace("placeholder", blacklistTwo))));
			assertThat(notifications.notificationNumber(2).getText(), anyOf(is(blacklistNotificationText.replace("placeholder", blacklistOne)), is(blacklistNotificationText.replace("placeholder", blacklistTwo))));
			assertThat(notifications.notificationNumber(2).getText(), not(is(notifications.notificationNumber(1).getText())));
		} finally {
			keywordsPage.deleteKeywords();
		}
	}

	@Test
	public void testSpotlightPromotionNotifications(){
		PromotionService ps = getApplication().createPromotionService(getElementFactory());

		String promotionTrigger = "Maggle";
		String search = "Cole";
		String promotionNotificationText = "Created a new spotlight promotion: Spotlight for: "+promotionTrigger;

		ps.setUpPromotion(new SpotlightPromotion(promotionTrigger), new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 2);
		try {
			getElementFactory().getSearchPage();
			checkForNotification(promotionNotificationText);
		} finally {
			ps.deleteAll();
		}
	}

	@Test
	public void testRemovingSpotlightPromotionNotifications(){
		PromotionService ps = getApplication().createPromotionService(getElementFactory());

		String promotionTrigger = "lack";
		String search = "colour";
		String promotionNotificationText = "Removed a spotlight promotion";

		SpotlightPromotion spotlightPromotion = new SpotlightPromotion(promotionTrigger);

		ps.setUpPromotion(spotlightPromotion, new SearchActionFactory(getApplication(),getElementFactory()).makeSearch(search),2);
		ps.delete(spotlightPromotion);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testPinToPositionPromotionNotifications(){
		PromotionService ps = getApplication().createPromotionService(getElementFactory());

		int pinToPositionPosition = 1;
		String promotionTrigger = "Ziggler".toLowerCase();
		String search = "Cena".toLowerCase();
		String promotionNotificationText = "Created a new pin to position promotion: Pin to Position for: "+promotionTrigger;

		ps.setUpPromotion(new PinToPositionPromotion(pinToPositionPosition, promotionTrigger), new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		try {
			getElementFactory().getSearchPage();
			checkForNotification(promotionNotificationText);
		} finally {
			ps.deleteAll();
		}
	}

	@Test
	public void testRemovingPinToPositionPromotionNotifications(){
		PromotionService ps = getApplication().createPromotionService(getElementFactory());

		int pinToPositionPosition = 1;
		String promotionTrigger = "Ziggler".toLowerCase();
		String search = "Cena".toLowerCase();
		String promotionNotificationText = "Removed a pin to position promotion";

		PinToPositionPromotion ptpp = new PinToPositionPromotion(pinToPositionPosition,promotionTrigger);

		ps.setUpPromotion(ptpp, new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		ps.delete(ptpp);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testDynamicPromotionNotifications(){
		PromotionService ps = getApplication().createPromotionService(getElementFactory());

		int numberOfResults = 10;
		String promotionTrigger = "Wyatt".toLowerCase();
		String search = "Lawler".toLowerCase();
		String promotionNotificationText = "Created a new dynamic spotlight promotion: Dynamic Spotlight for: " + promotionTrigger;

		ps.setUpPromotion(new DynamicPromotion(numberOfResults, promotionTrigger), new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		try {
			getElementFactory().getSearchPage();
			checkForNotification(promotionNotificationText);
		} finally {
			ps.deleteAll();
		}
	}

	@Test
	public void testRemovingDynamicPromotionNotifications(){
		PromotionService ps = getApplication().createPromotionService(getElementFactory());

		int numberOfResults = 10;
		String promotionTrigger = "Wyatt".toLowerCase();
		String search = "Lawler".toLowerCase();
		String promotionNotificationText = "Removed a dynamic spotlight promotion";

		DynamicPromotion dynamic = new DynamicPromotion(numberOfResults, promotionTrigger);

		ps.setUpPromotion(dynamic, new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		ps.delete(dynamic);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testDeletingSynonymsNotifications() throws InterruptedException {
		String synonymOne = "Dean".toLowerCase();
		String synonymTwo = "Ambrose".toLowerCase();
		String synonymThree = "Shield".toLowerCase();

		//Have to add synonyms first before deleting them
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		getElementFactory().getKeywordsPage().createNewKeywordsButton().click();
		getElementFactory().getCreateNewKeywordsPage().createSynonymGroup(synonymOne + " " + synonymTwo + " " + synonymThree, "English");
		getElementFactory().getSearchPage();
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();

		try {
			//Now try deleting
			String removeSynonymOneNotification = "Removed \"" + synonymOne + "\" from a synonym group";
			keywordsPage.deleteSynonym(synonymOne, synonymOne);
			checkForNotification(removeSynonymOneNotification);
			body.getTopNavBar().notificationsDropdown(); //Close notifications dropdown
			String removeSynonymGroupNotification = "Removed a synonym group";
			keywordsPage.deleteSynonym(synonymTwo, synonymTwo);
			checkForNotification(removeSynonymGroupNotification);
		} finally {
			keywordsPage.deleteKeywords();
		}
	}

	@Test
	public void testDeletingBlacklistNotifications() throws InterruptedException {
		String blacklistOne = "Rollins".toLowerCase();
		String blacklistTwo = "Seth".toLowerCase();
		String blacklistNotificationText = "Removed \"placeholder\" from the blacklist";

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.createNewKeywordsButton().click();
		getElementFactory().getCreateNewKeywordsPage().createBlacklistedTerm(blacklistOne + " " + blacklistTwo, "English");

		keywordsPage = getElementFactory().getKeywordsPage();
		try {
			keywordsPage.deleteBlacklistedTerm(blacklistOne);        //The gritter happens during this phase so cannot wait to check if gritter is okay afterward
			body.getTopNavBar().notificationsDropdown();
			notifications = body.getTopNavBar().getNotifications();
			assertThat(notifications.notificationNumber(1).getText(), is(blacklistNotificationText.replace("placeholder", blacklistOne)));
			keywordsPage.deleteBlacklistedTerm(blacklistTwo);
			assertThat(notifications.notificationNumber(1).getText(), is(blacklistNotificationText.replace("placeholder", blacklistTwo)));
		} finally {
			keywordsPage.deleteKeywords();
		}
	}
}
