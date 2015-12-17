package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordWizardType;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

/*
 *   IMPORTANT - HOSTED ALLOWS CAPITALS IN PROMOTION TITLES/TRIGGERS WHILE OP DOESN'T (hence the .toLowerCase() everywhere so I don't forget
 */
public class NotificationsDropDownITCase extends NotificationsDropDownTestBase {
	private KeywordService keywordService;
	private PromotionService promotionService;

	public NotificationsDropDownITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	@Before
	public void serviceSetUp() {
		keywordService = new KeywordService(getApplication(), getElementFactory());
		promotionService = getApplication().createPromotionService(getElementFactory());
	}

	@Test
	public void testCountNotifications() throws InterruptedException {
		keywordService.addSynonymGroup("john juan jO");
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
		keywordService.deleteAll(KeywordFilter.ALL);
		try {
			keywordService.addBlacklistTerms("one two three four five");
			topNavBar.notificationsDropdown();
			notifications = topNavBar.getNotifications();
			assertThat("5 notifications before page refresh", notifications.countNotifications(), is(5));

			getDriver().navigate().refresh();
			newBody();
			body.getTopNavBar().notificationsDropdown();
			notifications = topNavBar.getNotifications();
			assertThat("5 notifications after page refresh", notifications.countNotifications(), is(5));
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
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
			keywordService.addSynonymGroup("Animal Beast");
			keywordsPage = keywordService.goToKeywords();

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
				keywordService.addSynonymGroup(i + " " + (i + 1));
				keywordService.goToKeywords();

				bodyWindowTwo.getTopNavBar().notificationsDropdown();
				verifyThat(notificationsDropDownWindowTwo.countNotifications(), is(Math.min(++notificationsCount, 5)));
				notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

				getDriver().switchTo().window(browserHandles.get(0));
				verifyThat(notifications.countNotifications(), is(Math.min(notificationsCount, 5)));
				verifyThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));
			}
		} finally {
			getDriver().switchTo().window(browserHandles.get(1));
			topNavBarWindowTwo.closeNotifications();
			keywordService.deleteAll(KeywordFilter.ALL);
			promotionService.deleteAll();
		}
	}

	@Test
	public void testSynonymNotifications() throws InterruptedException {
		String synonymOne = "Brock".toLowerCase();
		String synonymTwo = "Lesnar".toLowerCase();
		String synonymNotificationText = "Created a new synonym group containing: "+synonymOne+", "+synonymTwo;

		// waiting for the notification - just do the wizard
		keywordService.addKeywords(KeywordWizardType.SYNONYMS, Language.ENGLISH, Arrays.asList(synonymOne, synonymTwo));

		try {
			checkForNotification(synonymNotificationText);
		} finally {
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}

	@Test
	public void testBlacklistNotifications() throws InterruptedException {
		String blacklistOne = "seth";
		String blacklistTwo = "rollins";
		String blacklistNotificationText = "Added \"placeholder\" to the blacklist";

		keywordService.deleteAll(KeywordFilter.ALL);
		keywordService.addBlacklistTerms(blacklistOne, blacklistTwo);
		try {
			body.getTopNavBar().notificationsDropdown();
			notifications = body.getTopNavBar().getNotifications();
			assertThat(notifications.notificationNumber(1).getText(), anyOf(is(blacklistNotificationText.replace("placeholder", blacklistOne)), is(blacklistNotificationText.replace("placeholder", blacklistTwo))));
			assertThat(notifications.notificationNumber(2).getText(), anyOf(is(blacklistNotificationText.replace("placeholder", blacklistOne)), is(blacklistNotificationText.replace("placeholder", blacklistTwo))));
			assertThat(notifications.notificationNumber(2).getText(), not(is(notifications.notificationNumber(1).getText())));
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}

	@Test
	public void testSpotlightPromotionNotifications(){
		String promotionTrigger = "blatter";
		String search = "thief";
		String promotionNotificationText = "Created a new spotlight promotion: Spotlight for: "+promotionTrigger;

		promotionService.setUpPromotion(new SpotlightPromotion(promotionTrigger), new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 2);
		try {
			getElementFactory().getSearchPage();
			checkForNotification(promotionNotificationText);
		} finally {
			promotionService.deleteAll();
		}
	}

	@Test
	public void testRemovingSpotlightPromotionNotifications(){
		String promotionTrigger = "lack";
		String search = "colour";
		String promotionNotificationText = "Removed a spotlight promotion";

		SpotlightPromotion spotlightPromotion = new SpotlightPromotion(promotionTrigger);

		promotionService.setUpPromotion(spotlightPromotion, new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 2);
		promotionService.delete(spotlightPromotion);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testPinToPositionPromotionNotifications(){
		int pinToPositionPosition = 1;
		String promotionTrigger = "Ziggler".toLowerCase();
		String search = "Cena".toLowerCase();
		String promotionNotificationText = "Created a new pin to position promotion: Pin to Position for: "+promotionTrigger;

		promotionService.setUpPromotion(new PinToPositionPromotion(pinToPositionPosition, promotionTrigger), new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		try {
			getElementFactory().getSearchPage();
			checkForNotification(promotionNotificationText);
		} finally {
			promotionService.deleteAll();
		}
	}

	@Test
	public void testRemovingPinToPositionPromotionNotifications(){
		int pinToPositionPosition = 1;
		String promotionTrigger = "Ziggler".toLowerCase();
		String search = "Cena".toLowerCase();
		String promotionNotificationText = "Removed a pin to position promotion";

		PinToPositionPromotion ptpp = new PinToPositionPromotion(pinToPositionPosition,promotionTrigger);

		promotionService.setUpPromotion(ptpp, new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		promotionService.delete(ptpp);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testDynamicPromotionNotifications(){
		int numberOfResults = 10;
		String promotionTrigger = "football";
		String search = "soccer";
		String promotionNotificationText = "Created a new dynamic spotlight promotion: Dynamic Spotlight for: " + promotionTrigger;

		promotionService.setUpPromotion(new DynamicPromotion(numberOfResults, promotionTrigger), new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		try {
			getElementFactory().getSearchPage();
			checkForNotification(promotionNotificationText);
		} finally {
			promotionService.deleteAll();
		}
	}

	@Test
	public void testRemovingDynamicPromotionNotifications(){
		int numberOfResults = 10;
		String promotionTrigger = "platini";
		String search = "liar";
		String promotionNotificationText = "Removed a dynamic spotlight promotion";

		DynamicPromotion dynamic = new DynamicPromotion(numberOfResults, promotionTrigger);

		promotionService.setUpPromotion(dynamic, new SearchActionFactory(getApplication(), getElementFactory()).makeSearch(search), 1);
		promotionService.delete(dynamic);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testDeletingSynonymsNotifications() throws InterruptedException {
		String synonymOne = "Dean".toLowerCase();
		String synonymTwo = "Ambrose".toLowerCase();
		String synonymThree = "Shield".toLowerCase();

		//Have to add synonyms first before deleting them
		keywordService.addSynonymGroup(synonymOne, synonymTwo, synonymThree);
		keywordsPage = keywordService.goToKeywords();

		try {
			String removeSynonymOneNotification = "Removed \"" + synonymOne + "\" from a synonym group";
			keywordsPage.synonymGroupContaining(synonymOne).synonymBox(synonymOne).removeAsync();
			checkForNotification(removeSynonymOneNotification);
			body.getTopNavBar().notificationsDropdown(); //Close notifications dropdown
			String removeSynonymGroupNotification = "Removed a synonym group";
			keywordsPage.synonymGroupContaining(synonymTwo).synonymBox(synonymTwo).removeAsync();
			checkForNotification(removeSynonymGroupNotification);
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}

	@Test
	public void testDeletingBlacklistNotifications() throws InterruptedException {
		String blacklistOne = "Rollins".toLowerCase();
		String blacklistTwo = "Seth".toLowerCase();
		String blacklistNotificationText = "Removed \"placeholder\" from the blacklist";

		keywordsPage = keywordService.addBlacklistTerms(blacklistOne, blacklistTwo);

		try {
			keywordsPage.deleteBlacklistedTerm(blacklistOne);        //The gritter happens during this phase so cannot wait to check if gritter is okay afterward
			body.getTopNavBar().notificationsDropdown();
			notifications = body.getTopNavBar().getNotifications();
			assertThat(notifications.notificationNumber(1).getText(), is(blacklistNotificationText.replace("placeholder", blacklistOne)));
			keywordsPage.deleteBlacklistedTerm(blacklistTwo);
			assertThat(notifications.notificationNumber(1).getText(), is(blacklistNotificationText.replace("placeholder", blacklistTwo)));
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}
}
