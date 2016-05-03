package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.analytics.DashboardBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;

public class MultiWindowNotificationsITCase extends HybridIsoTestBase {
    private KeywordService keywordService;
    private PromotionService promotionService;

    private AppWindow first;
    private Window mainWindow;
    private TopNavBar topNavBar;
    private NotificationsDropDown notifications;

    private AppWindow second;
    private IsoApplication<?> secondApp;
    private Window secondWindow;
    private TopNavBar topNavBarWindowTwo;
    private NotificationsDropDown notificationsDropDownWindowTwo;

    public MultiWindowNotificationsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        keywordService = getApplication().keywordService();
        promotionService = getApplication().promotionService();

        mainWindow = getWindow();
        topNavBar = getElementFactory().getTopNavBar();
        first = new AppWindow(getApplication(), mainWindow);

        secondApp = IsoApplication.ofType(getConfig().getType());
        secondWindow = launchInNewWindow(secondApp);
        secondWindow.activate();
        topNavBarWindowTwo = secondApp.elementFactory().getTopNavBar();
        second = new AppWindow(secondApp, secondWindow);
    }

    @After
    public void tearDown() {
        if (hasSetUp()) {
            secondWindow.activate();
            topNavBarWindowTwo.closeNotifications();
            keywordService.deleteAll(KeywordFilter.ALL);
            promotionService.deleteAll();
        }
    }

    @Test
    @KnownBug("CSA-1542")
    public void testNotificationsOverTwoWindows() throws InterruptedException {
        mainWindow.activate();
        keywordService.goToKeywords();
        topNavBar.notificationsDropdown();
        notifications = topNavBar.getNotifications();
        assertThat(notifications.countNotifications(), is(0));

        secondWindow.activate();
        secondApp.keywordService().goToKeywords();
        topNavBarWindowTwo.notificationsDropdown();
        notificationsDropDownWindowTwo = topNavBarWindowTwo.getNotifications();
        assertThat(notificationsDropDownWindowTwo.countNotifications(), is(0));

        mainWindow.activate();
        keywordService.addSynonymGroup("Animal Beast");
        keywordService.goToKeywords();

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

            secondApp.elementFactory().getTopNavBar().notificationsDropdown();
            verifyThat(notificationsDropDownWindowTwo.countNotifications(), is(Math.min(++notificationsCount, 5)));
            notificationMessages = notificationsDropDownWindowTwo.getAllNotificationMessages();

            mainWindow.activate();
            verifyThat(notifications.countNotifications(), is(Math.min(notificationsCount, 5)));
            verifyThat(notifications.getAllNotificationMessages(), contains(notificationMessages.toArray()));
        }
    }

    private void newBody() {
        topNavBar = getElementFactory().getTopNavBar();
    }

    private static class AppWindow {
        private final IsoApplication<?> app;
        private final Window window;

        AppWindow(IsoApplication<?> app, Window window) {
            this.app = app;
            this.window = window;
        }
    }
}
