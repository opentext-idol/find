package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import org.junit.Before;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NotificationsDropDownTestBase extends ABCTestBase {
    protected com.autonomy.abc.selenium.menu.NotificationsDropDown notifications;
    protected KeywordsPage keywordsPage;
    protected TopNavBar topNavBar;

    public NotificationsDropDownTestBase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() throws InterruptedException {
        Thread.sleep(5000);
        topNavBar = getElementFactory().getTopNavBar();
        notifications = topNavBar.getNotifications();
    }

    protected void checkForNotificationNoWait(String notificationText) {
        checkForNotificationNoWait(notificationText, 1);
    }

    protected void checkForNotificationNoWait(String notificationText, int notificationNumber) {
        getElementFactory().getTopNavBar().openNotifications();
        notifications = getElementFactory().getTopNavBar().getNotifications();
        assertThat(notifications.notificationNumber(notificationNumber).getText(), is(notificationText));
    }

    protected void checkForNotification(String notificationText) {
        new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationContaining(notificationText));
        checkForNotificationNoWait(notificationText);
    }

    protected void newBody(){
        topNavBar = getElementFactory().getTopNavBar();
    }
}
