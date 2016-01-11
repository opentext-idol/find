package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import org.junit.Before;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NotificationsDropDownTestBase extends ABCTestBase {
    protected com.autonomy.abc.selenium.menu.NotificationsDropDown notifications;
    protected KeywordsPage keywordsPage;
    protected TopNavBar topNavBar;
    protected SideNavBar sideNavBar;

    public NotificationsDropDownTestBase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp() throws InterruptedException {
        Thread.sleep(5000);
        topNavBar = body.getTopNavBar();
        sideNavBar = body.getSideNavBar();
        notifications = topNavBar.getNotifications();
    }

    protected void checkForNotification(String notificationText, boolean wait) {
        if (wait) {
            new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationContaining(notificationText));
        }
        body.getTopNavBar().notificationsDropdown();
        notifications = body.getTopNavBar().getNotifications();
        assertThat(notifications.notificationNumber(1).getText(), is(notificationText));
    }

    protected void newBody(){
        body = getBody();
        topNavBar = body.getTopNavBar();
        sideNavBar = body.getSideNavBar();
    }
}
