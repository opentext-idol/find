package com.autonomy.abc.shared;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NotificationTestHelper {
    private final IsoApplication<?> app;
    private TopNavBar topNavBar;

    public NotificationTestHelper(final IsoApplication<?> app) {
        this.app = app;
    }

    public void checkForNotificationNoWait(final String notificationText) {
        checkForNotificationNoWait(notificationText, 1);
    }

    public void checkForNotificationNoWait(final String notificationText, final int notificationNumber) {
        refreshNavBar();
        topNavBar.openNotifications();
        final NotificationsDropDown notifications = topNavBar.getNotifications();
        assertThat(notifications.notificationNumber(notificationNumber).getText(), is(notificationText));
    }

    public void checkForNotification(final String notificationText) {
        new WebDriverWait(app.elementFactory().getDriver(), 10)
                .until(GritterNotice.notificationContaining(notificationText));
        checkForNotificationNoWait(notificationText);
    }

    private void refreshNavBar(){
        topNavBar = app.elementFactory().getTopNavBar();
    }

}
