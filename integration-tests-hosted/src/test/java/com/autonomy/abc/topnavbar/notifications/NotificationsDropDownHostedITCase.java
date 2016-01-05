package com.autonomy.abc.topnavbar.notifications;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.Notification;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import com.autonomy.abc.selenium.page.login.GoogleAuth;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;

public class NotificationsDropDownHostedITCase extends NotificationsDropDownTestBase {
    public NotificationsDropDownHostedITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
        super(config, browser, appType, platform);
        setInitialUser(config.getUser("index_tests"));
    }

    @Override
    public HSOElementFactory getElementFactory() {
        return (HSOElementFactory) super.getElementFactory();
    }

    @Test
    public void testStaticPromotionNotifications(){
        HSOPromotionService ps = (HSOPromotionService) getApplication().createPromotionService(getElementFactory());

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
        HSOPromotionService ps = (HSOPromotionService) getApplication().createPromotionService(getElementFactory());

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
    public void testCreateIndexNotifications() {
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexes = getElementFactory().getIndexesPage();
        body = getBody();

        String indexName = "danye west";
        String indexCreationNotification = "Created a new index: "+indexName;

        try {
            indexes.newIndexButton().click();
            CreateNewIndexPage createNewIndexPage = getElementFactory().getCreateNewIndexPage();
            createNewIndexPage.inputIndexName(indexName);
            createNewIndexPage.nextButton().click();
            Waits.loadOrFadeWait();
            createNewIndexPage.nextButton().click();
            Waits.loadOrFadeWait();
            createNewIndexPage.finishButton().click();

            new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining(indexCreationNotification));

            checkForNotification(indexCreationNotification);
        } finally {
            body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
            getElementFactory().getIndexesPage().deleteIndex(indexName);
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

        ConnectionService cs = new ConnectionService(getApplication(), getElementFactory());
        try {
            cs.setUpConnection(connector); //Notifications are dealt with within here, so need to wait for them

            getElementFactory().getConnectionsPage();

            body.getTopNavBar().notificationsDropdown();
            notifications = body.getTopNavBar().getNotifications();

            assertThat(notifications.notificationNumber(1).getText(), is(finishedNotification));
            assertThat(notifications.notificationNumber(2).getText(), is(startedNotification));
            assertThat(notifications.notificationNumber(3).getText(), is(createdNotification));
            assertThat(notifications.notificationNumber(4).getText(), is(creatingNotification));
        } finally {
            cs.deleteConnection(connector, true);
        }
    }

    @Test
    public void testConnectorsDeletionNotifications() {
        String connectorName = "deathcabyoucutie";
        WebConnector connector = new WebConnector("http://deathcabforcutie.com/", connectorName).withDuration(60);

        String deletingNotification = "Deleting connection " + connectorName;
        String successfulNotification = "Connection " + connectorName + " successfully removed";

        ConnectionService cs = new ConnectionService(getApplication(), getElementFactory());
        cs.setUpConnection(connector);

        cs.deleteConnection(connector, true);        //Because of the WebDriverWait within no need to wait for the notifications

        body.getTopNavBar().notificationsDropdown();
        notifications = body.getTopNavBar().getNotifications();

        assertThat(notifications.notificationNumber(1).getText(), is(successfulNotification));
        assertThat(notifications.notificationNumber(2).getText(), is(deletingNotification));
    }

    @Test
    //CSA-1698 || CSA-1687
    public void testUsernameShowsInNotifications() throws Exception {
        body.getSideNavBar().switchPage(NavBarTabId.DEVELOPERS);
        String devUsername = getElementFactory().getDevsPage().getUsernames().get(0);

        KeywordService keywordService = new KeywordService(getApplication(), getElementFactory());
        UserService userService = getApplication().createUserService(getElementFactory());
        WebDriver adminDriver = null;

        SignupEmailHandler emailHandler = new GmailSignupEmailHandler((GoogleAuth) config.getUser("google").getAuthProvider());

        try {
            keywordService.addSynonymGroup("My", "Good", "Friend", "Jeff");

            body.getTopNavBar().notificationsDropdown();
            for (Notification notification : body.getTopNavBar().getNotifications().getAllNotifications()) {
                verifyThat(notification.getUsername(), is(devUsername));
            }

            User user = userService.createNewUser(config.getNewUser("drake"), Role.ADMIN);

            try {
                user.authenticate(config.getWebDriverFactory(), emailHandler);
            } catch (TimeoutException e) {
                /* User has likely already been authenticated recently, attempt to continue */
            }

            adminDriver = config.getWebDriverFactory().create();
            adminDriver.get(config.getWebappUrl());

            adminDriver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);

            new HSOLoginPage(adminDriver, new AbcHasLoggedIn(adminDriver)).loginWith(user.getAuthProvider());

            HSOElementFactory elementFactory = new HSOElementFactory(adminDriver);
            elementFactory.getPromotionsPage();
            AppBody secondScreen = getApplication().createAppBody(adminDriver);

            new KeywordService(getApplication(),elementFactory).addSynonymGroup("Messi", "Campbell");

            verifyThat(body.getTopNavBar().getNotifications().getNotification(1).getUsername(), is(user.getUsername()));
            secondScreen.getTopNavBar().notificationsDropdown();
            verifyThat(secondScreen.getTopNavBar().getNotifications().getNotification(1).getUsername(), is(user.getUsername()));

            keywordService.addSynonymGroup("Joel", "Lionel");

            verifyThat(body.getTopNavBar().getNotifications().getNotification(1).getUsername(), is(devUsername));
            verifyThat(secondScreen.getTopNavBar().getNotifications().getNotification(1).getUsername(), is(devUsername));

        } finally {
            if(adminDriver != null) {
                adminDriver.quit();
            }

            userService.deleteOtherUsers();
            keywordService.deleteAll(KeywordFilter.ALL);

            emailHandler.markAllEmailAsRead(getDriver());
        }
    }

    @Test
    //CSA-1583
    public void testNotificationsPersistOverPages(){
        KeywordService keywordService = getApplication().createKeywordService(getElementFactory());

        keywordService.addSynonymGroup("Pop", "Punk");
        keywordService.addBlacklistTerms("Shola", "Ameobi");
        keywordService.deleteAll(KeywordFilter.ALL);

        body.getTopNavBar().notificationsDropdown();
        List<Notification> notifications = body.getTopNavBar().getNotifications().getAllNotifications();

        for(NavBarTabId page : Arrays.asList(NavBarTabId.ANALYTICS, NavBarTabId.CONNECTIONS, NavBarTabId.PROMOTIONS, NavBarTabId.KEYWORDS, NavBarTabId.USERS)){
            navigateAndVerifyNotifications(page, notifications);
        }
    }

    private void navigateAndVerifyNotifications(NavBarTabId page, List<Notification> notifications){
        body.getSideNavBar().switchPage(page);
        getElementFactory().waitForPage(page);

        body = getBody();

        body.getTopNavBar().openNotifications();

        verifyThat(body.getTopNavBar().getNotifications().getAllNotifications(), contains(notifications.toArray()));
    }
}
