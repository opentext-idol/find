package com.autonomy.abc.topnavbar.notifications;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.Notification;
import com.autonomy.abc.selenium.promotions.HsodPromotionService;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.shared.NotificationsDropDownTestBase;
import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;

@RelatedTo("CSA-1583")
public class NotificationsDropDownHostedITCase extends NotificationsDropDownTestBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public NotificationsDropDownHostedITCase(final TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));
    }

    @Override
    public HSODElementFactory getElementFactory() {
        return (HSODElementFactory) super.getElementFactory();
    }

    @Override
    public HSODApplication getApplication() {
        return (HSODApplication) super.getApplication();
    }

    @Test
    public void testStaticPromotionNotifications(){
        HsodPromotionService ps = getApplication().promotionService();

        String docTitle = "TITLE";
        String docContent = "CONTENT";
        String promotionTrigger = "sadness";
        String promotionNotificationText = "Created a new static promotion: Static Promotion for: " + promotionTrigger;

        ps.setUpStaticPromotion(new StaticPromotion(docTitle, docContent, promotionTrigger));
        try {
            getElementFactory().getSearchPage();
            checkForNotification(promotionNotificationText);
        } finally {
            ps.deleteAll();
        }
    }

    @Test
    public void testRemovingStaticPromotionNotifications(){
        HsodPromotionService ps = getApplication().promotionService();

        String docTitle = "TITLE";
        String docContent = "CONTENT";
        String promotionTrigger = "sadness";
        String promotionNotificationText = "Removed a static promotion";

        StaticPromotion staticPromotion = new StaticPromotion(docTitle, docContent, promotionTrigger);

        ps.setUpStaticPromotion(staticPromotion);
        ps.delete(staticPromotion);

        checkForNotification(promotionNotificationText);
    }

    @Test
    @RelatedTo("CSA-2014")
    public void testCreateDeleteIndexNotifications() {
        Index noDisplay = new Index("danye west");
        verifyIndexNotifications(noDisplay, noDisplay.getName());
        Index display = new Index("something", "Display Name 123");
        verifyIndexNotifications(display, display.getDisplayName());
    }

    private void verifyIndexNotifications(Index index, String expectedName) {
        IndexService indexService = getApplication().indexService();
        try {
            indexService.setUpIndex(index);
            checkForNotificationNoWait("Created a new index: " + expectedName);
        } finally {
            indexService.deleteIndex(index);
            checkForNotificationNoWait("Index " + expectedName + " successfully deleted");
            checkForNotificationNoWait("Deleting index " + expectedName, 2);
        }
    }

    @Test
    public void testConnectorsCreationNotifications(){
        String connectorName = "lc";

        String creatingNotification = "Creating a new connection: " + connectorName;
        String createdNotification = "Created a new connection: " + connectorName;
        String startedNotification = "Connection " + connectorName + " started";
        String finishedNotification = "Connection "+ connectorName + " has finished running";

        WebConnector connector = new WebConnector("http://loscampesinos.com/", connectorName).withDuration(60);

        ConnectionService cs = getApplication().connectionService();
        try {
            cs.setUpConnection(connector); //Notifications are dealt with within here, so need to wait for them

            getElementFactory().getConnectionsPage();

            getElementFactory().getTopNavBar().notificationsDropdown();
            notifications = getElementFactory().getTopNavBar().getNotifications();

            assertThat(notifications.notificationNumber(1).getText(), is(finishedNotification));
            assertThat(notifications.notificationNumber(2).getText(), is(startedNotification));
            assertThat(notifications.notificationNumber(3).getText(), is(createdNotification));
            assertThat(notifications.notificationNumber(4).getText(), is(creatingNotification));
        } finally {
            cs.deleteConnection(connector, true);
        }
    }

    @Test
    @KnownBug("CSA-2043")
    public void testConnectorsDeletionNotifications() {
        String connectorName = "deathcabyoucutie";
        WebConnector connector = new WebConnector("http://deathcabforcutie.com/", connectorName).withDuration(60);

        String deletingNotification = "Deleting connection " + connectorName;
        String successfulNotification = "Connection " + connectorName + " successfully removed";

        ConnectionService cs = getApplication().connectionService();
        cs.setUpConnection(connector);

        cs.deleteConnection(connector, true);        //Because of the WebDriverWait within no need to wait for the notifications

        getElementFactory().getTopNavBar().notificationsDropdown();
        notifications = getElementFactory().getTopNavBar().getNotifications();

        assertThat(notifications.notificationNumber(1).getText(), is(successfulNotification));
        assertThat(notifications.notificationNumber(2).getText(), is(deletingNotification));
    }

    // TODO: this is a mess
    @Test
    @KnownBug({"CSA-1698", "CSA-1687"})
    public void testUsernameShowsInNotifications() throws Exception {
        KeywordService keywordService = getApplication().keywordService();
        UserService userService = getApplication().userService();
        AuthenticationStrategy authenticationStrategy = getConfig().getAuthenticationStrategy();
        Session secondSession = null;

        HsodDevelopersPage hsoDevelopersPage = getApplication().switchTo(HsodDevelopersPage.class);

        User dev = new User(null, hsoDevelopersPage.getUsernames().get(0));
        String devUsername = "Brendon Urie";
        hsoDevelopersPage.editUsername(dev, devUsername);

        try {
            addKeywordsAndVerifyNotifications(keywordService, devUsername);

            User user = userService.createNewUser(getConfig().getNewUser("drake"), Role.ADMIN);

            try {
                authenticationStrategy.authenticate(user);
            } catch (TimeoutException e) { /* User has likely already been authenticated recently, attempt to continue */ }

            HSODApplication secondApplication = new HSODApplication();
            secondSession = launchInNewSession(secondApplication);
            HSODElementFactory secondFactory = secondApplication.elementFactory();

            secondApplication.loginService().login(user);
            secondFactory.getPromotionsPage();

            secondApplication.keywordService().addSynonymGroup("Messi", "Campbell");
            secondFactory.getTopNavBar().openNotifications();
            verifyNotificationCorrectUsername(user.getUsername(), secondFactory);

            keywordService.addSynonymGroup("Joel", "Lionel");
            verifyNotificationCorrectUsername(devUsername, secondFactory);

        } finally {
            if (secondSession != null) {
                getSessionRegistry().endSession(secondSession);
            }

            userService.deleteOtherUsers();
            keywordService.deleteAll(KeywordFilter.ALL);

            getConfig().getAuthenticationStrategy().cleanUp(getDriver());
        }
    }

    private void addKeywordsAndVerifyNotifications(KeywordService keywordService, String devUsername){
        keywordService.addSynonymGroup("My", "Good", "Friend", "Jeff");

        getElementFactory().getTopNavBar().openNotifications();
        for (Notification notification : getElementFactory().getTopNavBar().getNotifications().getAllNotifications()) {
            verifyThat(notification.getUsername(), is(devUsername));
        }
    }

    private void verifyNotificationCorrectUsername(String username, SOElementFactory secondFactory){
        verifyThat(getElementFactory().getTopNavBar().getNotifications().getNotification(1).getUsername(), is(username));
        verifyThat(secondFactory.getTopNavBar().getNotifications().getNotification(1).getUsername(), is(username));
    }

    @Test
    @RelatedTo("CSA-1586")
    @KnownBug("CSA-1542")
    public void testNotificationsPersistOverPages(){
        KeywordService keywordService = getApplication().keywordService();
        keywordService.deleteAll(KeywordFilter.ALL);

        keywordService.addSynonymGroup("Pop", "Punk");
        keywordService.addBlacklistTerms("Shola", "Ameobi");
        keywordService.deleteAll(KeywordFilter.ALL);

        getElementFactory().getTopNavBar().notificationsDropdown();
        List<Notification> notifications = getElementFactory().getTopNavBar().getNotifications().getAllNotifications();

        for (Class<? extends AppPage> page : Arrays.asList(
                PromotionsPage.class,
                AnalyticsPage.class,
                KeywordsPage.class,
                ConnectionsPage.class,
                UsersPage.class
        )) {
            navigateAndVerifyNotifications(page, notifications);
        }
    }

    private void navigateAndVerifyNotifications(Class<? extends AppPage> page, List<Notification> notifications) {
        getApplication().switchTo(page);
        logger.info("on page " + page);

        getElementFactory().getTopNavBar().openNotifications();

        verifyThat(getElementFactory().getTopNavBar().getNotifications().getAllNotifications(), contains(notifications.toArray()));
    }
}
