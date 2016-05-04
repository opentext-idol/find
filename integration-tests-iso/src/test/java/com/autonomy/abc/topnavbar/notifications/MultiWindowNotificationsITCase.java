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
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

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
            second.getNavBar().closeNotifications();
            second.keywordService().deleteAll(KeywordFilter.ALL);
            second.promotionService().deleteAll();
        }
    }

    @Test
    @KnownBug("CSA-1542")
    public void testNotificationsOverTwoWindows() throws InterruptedException {
        first.keywordService().goToKeywords();
        second.keywordService().goToKeywords();
        notificationsCountsShouldBe(0);

        first.keywordService().addSynonymGroup("Animal Beast");
        first.keywordService().goToKeywords();
        checkFirstNotification();

        second.keywordService().goToKeywords().deleteSynonym("Animal");
        notificationsCountsShouldBe(2);

        first.switchTo(DashboardBase.class);
        notificationsCountsShouldBe(2);

        second.promotionService().setUpPromotion(new SpotlightPromotion("wheels"), "cars", 3);
        new WebDriverWait(getDriver(), 5).until(GritterNotice.notificationAppears());
        notificationsCountsShouldBe(3);
        assertThat(first.mostRecentNotification(), containsString("promotion"));

        int notificationsCount = 3;
        for(int i = 0; i < 6; i += 2) {
            second.keywordService().addSynonymGroup(i + " " + (i + 1));
            second.keywordService().goToKeywords();
            notificationsCountsShouldBe(Math.min(++notificationsCount, 5));
        }
    }

    private void notificationsCountsShouldBe(int expected) {
        checkNotificationsCountFrom(second, is(expected));
        List<String> secondNotifications = second.allNotifications();
        checkNotificationsCountFrom(first, is(expected));
        verifyThat(first.allNotifications(), containsItems(secondNotifications));
    }

    private void checkFirstNotification() {
        checkNotificationsCountFrom(first, is(1));
        String windowOneNotificationText = first.mostRecentNotification();
        checkNotificationsCountFrom(second, is(1));
        verifyThat(second.mostRecentNotification(), is(windowOneNotificationText));
        second.getNavBar().closeNotifications();
    }

    private void checkNotificationsCountFrom(AppWindow inst, Matcher<Integer> expectation) {
        inst.activate();
        inst.getNavBar().openNotifications();
        verifyThat(inst.countNotifications(), expectation);
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

        PromotionService<?> promotionService() {
            activate();
            return promotionService;
        }

        KeywordService keywordService() {
            activate();
            return keywordService;
        }

        void switchTo(Class<? extends AppPage> pageType) {
            activate();
            app.switchTo(pageType);
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
