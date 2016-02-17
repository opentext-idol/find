package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.analytics.DashboardBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordWizardType;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import org.junit.Before;
import org.junit.Test;
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

	public NotificationsDropDownITCase(final TestConfig config) {
		super(config);
	}

	@Before
	public void serviceSetUp() {
		keywordService = getApplication().keywordService();
		promotionService = getApplication().promotionService();
	}

	@Test
	public void testCountNotifications() throws InterruptedException {
		keywordService.addSynonymGroup("john juan jO");
		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("There should be 1 notification in the drop down", notifications.countNotifications(), is(1));

		keywordsPage = keywordService.goToKeywords();
		keywordsPage.deleteSynonym("john");

		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("There should be 2 notifications in the drop down", notifications.countNotifications(), is(2));

		keywordsPage = keywordService.goToKeywords();
		keywordsPage.deleteSynonym("juan");

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
			getElementFactory().getTopNavBar().notificationsDropdown();
			notifications = topNavBar.getNotifications();
			assertThat("5 notifications after page refresh", notifications.countNotifications(), is(5));
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}

	@Test
	@KnownBug("CSA-1542")
	public void testNotificationsOverTwoWindows() throws InterruptedException {
		keywordsPage = keywordService.goToKeywords();

		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat(notifications.countNotifications(), is(0));

		final Window mainWindow = getMainSession().getActiveWindow();
		final Window secondWindow = getMainSession().openWindow(config.getWebappUrl());

		secondWindow.activate();
		SearchOptimizerApplication<?> appTwo = SearchOptimizerApplication.ofType(config.getType()).inWindow(secondWindow);
		TopNavBar topNavBarWindowTwo = appTwo.elementFactory().getTopNavBar();

		appTwo.keywordService().goToKeywords();
		topNavBarWindowTwo.notificationsDropdown();
		NotificationsDropDown notificationsDropDownWindowTwo = topNavBarWindowTwo.getNotifications();
		assertThat(notificationsDropDownWindowTwo.countNotifications(), is(0));

		try {
			mainWindow.activate();
			keywordService.addSynonymGroup("Animal Beast");
			keywordsPage = keywordService.goToKeywords();

			topNavBar.notificationsDropdown();
			notifications = topNavBar.getNotifications();
			assertThat(notifications.countNotifications(), is(1));
			String windowOneNotificationText = notifications.notificationNumber(1).getText();

			secondWindow.activate();
			assertThat(notificationsDropDownWindowTwo.countNotifications(), is(1));
			assertThat(notificationsDropDownWindowTwo.notificationNumber(1).getText(), is(windowOneNotificationText));
			topNavBarWindowTwo.notificationsDropdown();
			KeywordsPage keywordsPageWindowTwo = getElementFactory().getKeywordsPage();
			keywordsPageWindowTwo.deleteSynonym("Animal", "Animal");
			topNavBarWindowTwo.notificationsDropdown();
			assertThat(notificationsDropDownWindowTwo.countNotifications(), is(2));
			List<String> notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

			mainWindow.activate();
			assertThat(notifications.countNotifications(), is(2));
			assertThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));

			getApplication().switchTo(DashboardBase.class);
			newBody();
			topNavBar.notificationsDropdown();
			notifications = topNavBar.getNotifications();
			assertThat(notifications.countNotifications(), is(2));
			assertThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));

			secondWindow.activate();

			promotionService.setUpPromotion(new SpotlightPromotion("wheels"), "cars", 3);

			new WebDriverWait(getDriver(), 5).until(GritterNotice.notificationAppears());
			topNavBarWindowTwo.notificationsDropdown();
			notificationsDropDownWindowTwo = topNavBarWindowTwo.getNotifications();

			assertThat(notificationsDropDownWindowTwo.countNotifications(), is(3));
			assertThat(notificationsDropDownWindowTwo.notificationNumber(1).getText(), containsString("promotion"));

			notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

			mainWindow.activate();

			notifications = topNavBar.getNotifications();
			assertThat(notifications.countNotifications(), is(3));
			assertThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));

			int notificationsCount = 3;
			for(int i = 0; i < 6; i += 2) {
				secondWindow.activate();
				keywordService.addSynonymGroup(i + " " + (i + 1));
				keywordService.goToKeywords();

				appTwo.elementFactory().getTopNavBar().notificationsDropdown();
				verifyThat(notificationsDropDownWindowTwo.countNotifications(), is(Math.min(++notificationsCount, 5)));
				notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

				mainWindow.activate();
				verifyThat(notifications.countNotifications(), is(Math.min(notificationsCount, 5)));
				verifyThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));
			}
		} finally {
			secondWindow.activate();
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
			getElementFactory().getTopNavBar().notificationsDropdown();
			notifications = getElementFactory().getTopNavBar().getNotifications();
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

		promotionService.setUpPromotion(new SpotlightPromotion(promotionTrigger), search, 2);
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

		promotionService.setUpPromotion(spotlightPromotion, search, 2);
		promotionService.delete(spotlightPromotion);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testPinToPositionPromotionNotifications(){
		int pinToPositionPosition = 1;
		String promotionTrigger = "Ziggler".toLowerCase();
		String search = "Cena".toLowerCase();
		String promotionNotificationText = "Created a new pin to position promotion: Pin to Position for: "+promotionTrigger;

		promotionService.setUpPromotion(new PinToPositionPromotion(pinToPositionPosition, promotionTrigger), search, 1);
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

		promotionService.setUpPromotion(ptpp, search, 1);
		promotionService.delete(ptpp);

		checkForNotification(promotionNotificationText);
	}

	@Test
	public void testDynamicPromotionNotifications(){
		int numberOfResults = 10;
		String promotionTrigger = "football";
		String search = "soccer";
		String promotionNotificationText = "Created a new dynamic spotlight promotion: Dynamic Spotlight for: " + promotionTrigger;

		promotionService.setUpPromotion(new DynamicPromotion(numberOfResults, promotionTrigger), search, 1);
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

		promotionService.setUpPromotion(dynamic, search, 1);
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
			getElementFactory().getTopNavBar().notificationsDropdown(); //Close notifications dropdown
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
			getElementFactory().getTopNavBar().notificationsDropdown();
			notifications = getElementFactory().getTopNavBar().getNotifications();
			assertThat(notifications.notificationNumber(1).getText(), is(blacklistNotificationText.replace("placeholder", blacklistOne)));
			keywordsPage.deleteBlacklistedTerm(blacklistTwo);
			assertThat(notifications.notificationNumber(1).getText(), is(blacklistNotificationText.replace("placeholder", blacklistTwo)));
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}
}
