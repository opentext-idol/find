package com.autonomy.abc.shared;

import com.autonomy.abc.base.SOTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.junit.Before;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.TestStateAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NotificationsDropDownTestBase extends SOTestBase {
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
