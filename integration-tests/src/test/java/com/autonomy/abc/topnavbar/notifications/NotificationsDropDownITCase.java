package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import javafx.geometry.Side;
import org.apache.xpath.compiler.Keywords;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;

public class NotificationsDropDownITCase extends ABCTestBase{
	public NotificationsDropDownITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private NotificationsDropDown notifications;
	private KeywordsPage keywordsPage;
	private CreateNewKeywordsPage createNewKeywordsPage;
	private TopNavBar topNavBar;
	private SideNavBar sideNavBar;

	@Before
	public void setUp() {
		topNavBar = body.getTopNavBar();
		sideNavBar = body.getSideNavBar();
		notifications = topNavBar.getNotifications();
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

	private void newBody(){
		body = getBody();
		topNavBar = body.getTopNavBar();
		sideNavBar = body.getSideNavBar();
	}
}
