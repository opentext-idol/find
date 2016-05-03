package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.analytics.DashboardBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;

public class MultiWindowNotificationsITCase extends HybridIsoTestBase {
    private AppWindow first;
    private AppWindow second;

    public MultiWindowNotificationsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {

        first = new AppWindow(getApplication(), getWindow());

        IsoApplication<?> secondApp = IsoApplication.ofType(getConfig().getType());
        Window secondWindow = launchInNewWindow(secondApp);
        second = new AppWindow(secondApp, secondWindow);
    }

    @After
    public void tearDown() {
        if (hasSetUp()) {
            second.activate();
            second.closeNotifications();
            deleteAllKeywordsFrom(second);
            deleteAllPromotionsFrom(second);
        }
    }

    @Test
    @KnownBug("CSA-1542")
    public void testNotificationsOverTwoWindows() throws InterruptedException {
        goToKeywordsFrom(first);
        checkNotificationsCountFrom(first, is(0));

        goToKeywordsFrom(second);
        checkNotificationsCountFrom(second, is(0));

        addSynonymGroupFrom(first, "Animal Beast");
        goToKeywordsFrom(first);

        checkNotificationsCountFrom(first, is(1));
        String windowOneNotificationText = first.mostRecentNotification();

        checkNotificationsCountFrom(second, is(1));
        assertThat(second.mostRecentNotification(), is(windowOneNotificationText));
        second.closeNotifications();

        deleteSynonymFrom(second, "Animal");

        checkNotificationsCountFrom(second, is(2));
        List<String> notificationMessages = second.allNotifications();

        checkNotificationsCountFrom(first, is(2));
        assertThat(first.allNotifications(), contains(notificationMessages.toArray()));

        first.switchToDashboard();
        checkNotificationsCountFrom(first, is(2));
        assertThat(first.allNotifications(), contains(notificationMessages.toArray()));

        setUpPromotionFrom(second);
        checkNotificationsCountFrom(second, is(3));
        assertThat(second.mostRecentNotification(), containsString("promotion"));

        notificationMessages = second.allNotifications();

        checkNotificationsCountFrom(first, is(3));
        assertThat(first.allNotifications(), contains(notificationMessages.toArray()));

        int notificationsCount = 3;
        for(int i = 0; i < 6; i += 2) {
            addSynonymGroupFrom(second, i + " " + (i + 1));
            goToKeywordsFrom(second);

            checkNotificationsCountFrom(second, is(Math.min(++notificationsCount, 5)));
            notificationMessages = second.allNotifications();

            checkNotificationsCountFrom(first, is(Math.min(notificationsCount, 5)));
            verifyThat(first.allNotifications(), contains(notificationMessages.toArray()));
        }
    }

    private void setUpPromotionFrom(AppWindow inst) {
        inst.activate();
        inst.promotionService.setUpPromotion(new SpotlightPromotion("wheels"), "cars", 3);
        new WebDriverWait(getDriver(), 5).until(GritterNotice.notificationAppears());
    }

    private void deleteAllPromotionsFrom(AppWindow inst) {
        inst.activate();
        inst.promotionService.deleteAll();
    }

    private void goToKeywordsFrom(AppWindow inst) {
        inst.activate();
        inst.keywordService.goToKeywords();
    }

    private void addSynonymGroupFrom(AppWindow inst, String... synonyms) {
        inst.activate();
        inst.keywordService.addSynonymGroup(synonyms);
    }

    private void deleteSynonymFrom(AppWindow inst, String toDelete) {
        inst.activate();
        inst.keywordService.goToKeywords().deleteSynonym(toDelete);
    }

    private void deleteAllKeywordsFrom(AppWindow inst) {
        inst.activate();
        inst.keywordService.deleteAll(KeywordFilter.ALL);
    }

    private void checkNotificationsCountFrom(AppWindow inst, Matcher<Integer> expectation) {
        inst.activate();
        inst.openNotifications();
        assertThat(inst.countNotifications(), expectation);
    }

    private static class AppWindow {
        private final IsoApplication<?> app;
        private final Window window;
        private final KeywordService keywordService;
        private final PromotionService<?> promotionService;

        AppWindow(IsoApplication<?> app, Window window) {
            this.app = app;
            this.window = window;
            keywordService = app.keywordService();
            promotionService = app.promotionService();
        }

        void activate() {
            window.activate();
        }

        void switchToDashboard() {
            app.switchTo(DashboardBase.class);
        }

        void openNotifications() {
            getNavBar().openNotifications();
        }

        void closeNotifications() {
            getNavBar().closeNotifications();
        }

        int countNotifications() {
            return getNavBar().getNotifications().countNotifications();
        }

        List<String> allNotifications() {
            return getNavBar().getNotifications().getAllNotificationMessages();
        }

        String mostRecentNotification() {
            return getNavBar().getNotifications().notificationNumber(1).getText();
        }

        private TopNavBar getNavBar() {
            return app.elementFactory().getTopNavBar();
        }
    }
}
