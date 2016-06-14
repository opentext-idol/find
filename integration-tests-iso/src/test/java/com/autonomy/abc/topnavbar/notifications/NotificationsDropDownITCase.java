package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordWizardType;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.shared.NotificationTestHelper;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.*;

/*
 *   IMPORTANT - HOSTED ALLOWS CAPITALS IN PROMOTION TITLES/TRIGGERS WHILE OP DOESN'T (hence the .toLowerCase() everywhere so I don't forget
 */
public class NotificationsDropDownITCase extends HybridIsoTestBase {
	private final NotificationTestHelper helper;

	private KeywordService keywordService;
	private PromotionService promotionService;

	private TopNavBar topNavBar;
	private NotificationsDropDown notifications;

	public NotificationsDropDownITCase(final TestConfig config) {
		super(config);
		helper = new NotificationTestHelper(getApplication());
	}

	@Before
	public void setUp() {
		keywordService = getApplication().keywordService();
		promotionService = getApplication().promotionService();
		topNavBar = getElementFactory().getTopNavBar();
	}

	@Test
	public void testCountNotifications() throws InterruptedException {
		keywordService.addSynonymGroup("john juan jO");
		topNavBar.notificationsDropdown();
		notifications = topNavBar.getNotifications();
		assertThat("There should be 1 notification in the drop down", notifications.countNotifications(), is(1));

		KeywordsPage keywordsPage = keywordService.goToKeywords();
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

			getWindow().refresh();
			newBody();
			getElementFactory().getTopNavBar().notificationsDropdown();
			notifications = topNavBar.getNotifications();
			assertThat("5 notifications after page refresh", notifications.countNotifications(), is(5));
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}

	@Test
	public void testSynonymNotifications() throws InterruptedException {
		final String synonymOne = "Brock".toLowerCase();
		final String synonymTwo = "Lesnar".toLowerCase();
		final String synonymNotificationText = "Created a new synonym group containing: "+synonymOne+", "+synonymTwo;

		// waiting for the notification - just do the wizard
		keywordService.addKeywords(KeywordWizardType.SYNONYMS, Language.ENGLISH, Arrays.asList(synonymOne, synonymTwo));

		try {
			helper.checkForNotification(synonymNotificationText);
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}

	@Test
	public void testBlacklistNotifications() throws InterruptedException {
		final String blacklistOne = "seth";
		final String blacklistTwo = "rollins";
		final String blacklistNotificationText = "Added \"placeholder\" to the blacklist";

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
		final String promotionTrigger = "blatter";
		final String search = "thief";
		final String promotionNotificationText = "Created a new spotlight promotion: Spotlight for: "+promotionTrigger;

		promotionService.setUpPromotion(new SpotlightPromotion(promotionTrigger), search, 2);
		try {
			getElementFactory().getSearchPage();
			helper.checkForNotification(promotionNotificationText);
		} finally {
			promotionService.deleteAll();
		}
	}

	@Test
	public void testRemovingSpotlightPromotionNotifications(){
		final String promotionTrigger = "lack";
		final String search = "colour";
		final String promotionNotificationText = "Removed a spotlight promotion";

		final SpotlightPromotion spotlightPromotion = new SpotlightPromotion(promotionTrigger);

		promotionService.setUpPromotion(spotlightPromotion, search, 2);
		promotionService.delete(spotlightPromotion);

		helper.checkForNotification(promotionNotificationText);
	}

	@Test
	public void testPinToPositionPromotionNotifications(){
		final int pinToPositionPosition = 1;
		final String promotionTrigger = "Ziggler".toLowerCase();
		final String search = "Cena".toLowerCase();
		final String promotionNotificationText = "Created a new pin to position promotion: Pin to Position for: "+promotionTrigger;

		promotionService.setUpPromotion(new PinToPositionPromotion(pinToPositionPosition, promotionTrigger), search, 1);
		try {
			getElementFactory().getSearchPage();
			helper.checkForNotification(promotionNotificationText);
		} finally {
			promotionService.deleteAll();
		}
	}

	@Test
	public void testRemovingPinToPositionPromotionNotifications(){
		final int pinToPositionPosition = 1;
		final String promotionTrigger = "Ziggler".toLowerCase();
		final String search = "Cena".toLowerCase();
		final String promotionNotificationText = "Removed a pin to position promotion";

		final PinToPositionPromotion ptpp = new PinToPositionPromotion(pinToPositionPosition,promotionTrigger);

		promotionService.setUpPromotion(ptpp, search, 1);
		promotionService.delete(ptpp);

		helper.checkForNotification(promotionNotificationText);
	}

	@Test
	public void testDynamicPromotionNotifications(){
		final int numberOfResults = 10;
		final String promotionTrigger = "football";
		final String search = "soccer";
		final String promotionNotificationText = "Created a new dynamic spotlight promotion: Dynamic Spotlight for: " + promotionTrigger;

		promotionService.setUpPromotion(new DynamicPromotion(numberOfResults, promotionTrigger), search, 1);
		try {
			getElementFactory().getSearchPage();
			helper.checkForNotification(promotionNotificationText);
		} finally {
			promotionService.deleteAll();
		}
	}

	@Test
	public void testRemovingDynamicPromotionNotifications(){
		final int numberOfResults = 10;
		final String promotionTrigger = "platini";
		final String search = "liar";
		final String promotionNotificationText = "Removed a dynamic spotlight promotion";

		final DynamicPromotion dynamic = new DynamicPromotion(numberOfResults, promotionTrigger);

		promotionService.setUpPromotion(dynamic, search, 1);
		promotionService.delete(dynamic);

		helper.checkForNotification(promotionNotificationText);
	}

	@Test
	public void testDeletingSynonymsNotifications() throws InterruptedException {
		final String synonymOne = "Dean".toLowerCase();
		final String synonymTwo = "Ambrose".toLowerCase();
		final String synonymThree = "Shield".toLowerCase();

		//Have to add synonyms first before deleting them
		keywordService.addSynonymGroup(synonymOne, synonymTwo, synonymThree);
		final KeywordsPage keywordsPage = keywordService.goToKeywords();

		try {
			final String removeSynonymOneNotification = "Removed \"" + synonymOne + "\" from a synonym group";
			keywordsPage.synonymGroupContaining(synonymOne).synonymBox(synonymOne).removeAsync();
			helper.checkForNotification(removeSynonymOneNotification);
			getElementFactory().getTopNavBar().notificationsDropdown(); //Close notifications dropdown
			final String removeSynonymGroupNotification = "Removed a synonym group";
			keywordsPage.synonymGroupContaining(synonymTwo).synonymBox(synonymTwo).removeAsync();
			helper.checkForNotification(removeSynonymGroupNotification);
		} finally {
			keywordService.deleteAll(KeywordFilter.ALL);
		}
	}

	@Test
	public void testDeletingBlacklistNotifications() throws InterruptedException {
		final String blacklistOne = "Rollins".toLowerCase();
		final String blacklistTwo = "Seth".toLowerCase();
		final String blacklistNotificationText = "Removed \"placeholder\" from the blacklist";

		final KeywordsPage keywordsPage = keywordService.addBlacklistTerms(blacklistOne, blacklistTwo);

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

	private void newBody() {
		topNavBar = getElementFactory().getTopNavBar();
	}

	@Test
	@ResolvedBug("ISO-27")
	public void testNotificationsCloseOnPageSwitch(){
		topNavBar.openSettings();
		assertThat("Settings dropdown opened",topNavBar.settingsDropdownVisible());
		keywordService.goToKeywords();
		newBody();
		assertThat("Settings dropdown closed",!topNavBar.settingsDropdownVisible());
		promotionService.goToPromotions();
		newBody();
		assertThat("Settings dropdown closed",!topNavBar.settingsDropdownVisible());
		topNavBar.openSettings();
		assertThat("Settings dropdown opened",topNavBar.settingsDropdownVisible());
	}

	@Test
	@ResolvedBug("ISO-28")
	public void testNotificationsCloseOnIconClick(){
		topNavBar.openSettings();
		assertThat("Settings dropdown opened",topNavBar.settingsDropdownVisible());
		topNavBar.closeSettings();
		assertThat("Settings dropdown closed",!topNavBar.settingsDropdownVisible());

		topNavBar.openNotifications();
		assertThat("Notifications dropdown opened",topNavBar.notificationsDropdownVisible());
		topNavBar.closeNotifications();
		assertThat("Notifications dropdown closed",!topNavBar.notificationsDropdownVisible());

	}


	@Test
	@ResolvedBug("ISO-28")
	public void testNotificationsCloseOnAnywhereElseClick(){
		topNavBar.openSettings();
		assertThat("Settings dropdown opened",topNavBar.settingsDropdownVisible());

		topNavBar.openNotifications();
		assertThat("Settings dropdown closed",!topNavBar.settingsDropdownVisible());
		assertThat("Notifications dropdown opened",topNavBar.notificationsDropdownVisible());

		topNavBar.clickAnywhereButNotifications();
		assertThat("Settings dropdown closed",!topNavBar.settingsDropdownVisible());
		assertThat("Notifications dropdown closed",!topNavBar.notificationsDropdownVisible());
	}
}
