package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.ABCTearDown;
import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.external.GmailSignupEmailHandler;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexWizard;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.users.UsersPage;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("HOD-532")
public class UserPermissionsITCase extends HostedTestBase {
    public UserPermissionsITCase(TestConfig config) {
        super(config);
    }

    private UserService userService;
    private User user;

    private Session userSession;
    private HSODApplication userApp;
    private HSODElementFactory userElementFactory;

    private GmailSignupEmailHandler emailHandler;

    @Before
    public void setUp(){
        userService = getApplication().userService();
        GoogleAuth googleAuth = (GoogleAuth) userService.createNewUser(getConfig().generateNewUser(), Role.ADMIN).getAuthProvider();

        user = userService.createNewUser(getConfig().getNewUser("newhppassport"), Role.ADMIN);

        userApp = new HSODApplication();
        userSession = launchInNewSession(userApp);
        userElementFactory = userApp.elementFactory();

        emailHandler = new GmailSignupEmailHandler(googleAuth);

        user.authenticate(getConfig().getWebDriverFactory(), emailHandler);

        try {
            userApp.loginService().login(user);
        } catch (NoSuchElementException e) {
            try {
                assumeThat("Authentication failed", userSession.getDriver().getPageSource(), not(containsString("Authentication Failed")));
                assumeThat("Promotions page not displayed", userApp.elementFactory().getPromotionsPage(), displayed());
            } finally {
                tearDown();
            }
        }
    }

    @After
    public void tearDown(){
        ABCTearDown.USERS.tearDown(this);
        emailHandler.markAllEmailAsRead(getDriver());
    }

    @Test
    public void testCannotNavigate(){
        verifyThat(userElementFactory.getPromotionsPage(), displayed());

        userService.deleteUser(user);

        try {
            userApp.switchTo(AnalyticsPage.class);
        } catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e){
            //Expected as you'll be logged out
        }

        new WebDriverWait(userSession.getDriver(), 10).until(ExpectedConditions.titleIs("Haven Search OnDemand - Error"));

        verifyAuthFailed();
    }

    @Test
    public void testCannotAddKeywords(){
        String blacklist = "Dave";

        KeywordService keywordService = userApp.keywordService();
        KeywordsPage keywordsPage = keywordService.goToKeywords();
        keywordsPage.createNewKeywordsButton().click();
        CreateNewKeywordsPage createNewKeywordsPage = userElementFactory.getCreateNewKeywordsPage();

        Waits.loadOrFadeWait();

        userService.deleteUser(user);

        try {
            createNewKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
            createNewKeywordsPage.continueWizardButton().click();
            createNewKeywordsPage.getTriggerForm().addTrigger(blacklist);
            createNewKeywordsPage.finishWizardButton().click();

            verifyError();
        } catch (TimeoutException e) {
            try {
                verifyError();
            } catch (Exception f) {
                verifyAuthFailed();
            }
        } catch (StaleElementReferenceException e) {
            Waits.loadOrFadeWait();
            verifyAuthFailed();
        }

        getApplication().switchTo(KeywordsPage.class);

        verifyThat(getElementFactory().getKeywordsPage().getBlacklistedTerms(), not(hasItem(blacklist)));
    }

    @Test
    public void testCannotAddPromotions(){
        PromotionService promotionService = userApp.promotionService();
        userApp.switchTo(SearchPage.class);

        userService.deleteUser(user);

        try {
            promotionService.setUpPromotion(new SpotlightPromotion("BE ALONE"), "Baggins", 2);

            verifyError();
        } catch (StaleElementReferenceException | TimeoutException e) {
            verifyThat(userSession.getDriver().getPageSource(), anyOf(containsString("Authentication Failed"), containsString("Error executing search action")));
        }
    }

    @Test
    public void testCannotAddStaticPromotion(){
        HSODPromotionService promotionService = userApp.promotionService();
        HSODPromotionsPage promotionsPage = promotionService.goToPromotions();

        promotionsPage.staticPromotionButton().click();
        HSODCreateNewPromotionsPage createNewPromotionsPage = userElementFactory.getCreateNewPromotionsPage();

        userService.deleteUser(user);

        try {
            new StaticPromotion("TITLE", "CONTENT", "TRIGGER").makeWizard(createNewPromotionsPage).apply();

            verifyError();
        } catch (StaleElementReferenceException e) {
            verifyAuthFailed();
        }
    }

    @Test
    public void testCannotAddUser(){
        UserService userUserService = userApp.userService();
        UsersPage usersPage = userUserService.goToUsers();

        userService.deleteUser(user);

        try {
            usersPage.createUserButton().click();
            try {
                usersPage.addNewUser(getConfig().generateNewUser(), Role.ADMIN);
            } finally {
                usersPage.closeModal();
            }

           verifyError();
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            verifyAuthFailed();
        }
    }

    @Test
    public void testCannotAddIndex(){
        Index index = new Index("not gonna");
        CreateNewIndexPage createNewIndexPage = userApp.indexService().goToIndexWizard();

        userService.deleteUser(user);

        try {
            new IndexWizard(index, createNewIndexPage).apply();

            TopNavBar topNavBar = userElementFactory.getTopNavBar();
            topNavBar.openNotifications();
            NotificationsDropDown notificationsDropDown = topNavBar.getNotifications();

            verifyThat(notificationsDropDown.getAllNotificationMessages(), hasItems("Index " + index.getDisplayName() + " has not been created", "Unauthorized"));
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            verifyAuthFailed();
        } finally {
            IndexesPage devIndexesPage = getApplication().switchTo(IndexesPage.class);

            if(!verifyThat(devIndexesPage.getIndexDisplayNames(), not(hasItem(index.getDisplayName())))){
                getApplication().indexService().deleteIndex(index);
            }

            getApplication().switchTo(UsersPage.class);
        }
    }

    @Test
    public void testCannotAddConnector(){
        String connectorName = "abc";
        Connector connector = new WebConnector("http://www.google.co.uk", connectorName, Index.DEFAULT);

        ConnectionService connectionService = userApp.connectionService();

        connectionService.goToConnections();

        userService.deleteUser(user);

        try {
            connectionService.setUpConnection(connector);
        } catch (TimeoutException e) {
            TopNavBar topNavBar = userElementFactory.getTopNavBar();
            topNavBar.openNotifications();
            NotificationsDropDown notificationsDropDown = topNavBar.getNotifications();

            verifyThat(notificationsDropDown.getAllNotificationMessages(), hasItems("Failed to create a new connection: " + connectorName, "Failed to create '" + connectorName + "' connector", "Unauthorized"));
        } finally {
            ConnectionsPage connectionsPage = getApplication().switchTo(ConnectionsPage.class);

            if(!verifyThat(connectionsPage.getConnectionNames(), not(hasItem(connector.getName())))){
                getApplication().connectionService().deleteConnection(connector, false);
            }

            getApplication().switchTo(UsersPage.class);
        }
    }

    @Test
    @KnownBug("CSA-2116")
    public void testAnalyticsNotifications(){
        userApp.switchTo(IndexesPage.class);

        userService.deleteUser(user);

        userApp.switchTo(AnalyticsPage.class);

        TopNavBar topNavBar = userElementFactory.getTopNavBar();
        topNavBar.openNotifications();

        List<String> notifications = topNavBar.getNotifications().getAllNotificationMessages();

        assertThat(notifications, hasItem("Unauthorized"));

        int unauthorized = 0;
        for(String notification : notifications){
            if(notification.contains("Unauthorized")){
                unauthorized++;
            }
        }

        verifyThat("One 'unauthorized' notification is shown", unauthorized, is(1));
    }

    private void verifyError(){
        TopNavBar topNavBar = userElementFactory.getTopNavBar();
        topNavBar.openNotifications();
        NotificationsDropDown notificationsDropDown = topNavBar.getNotifications();
        verifyThat(notificationsDropDown.getNotification(1).getMessage(), containsString("Error"));
    }

    private void verifyAuthFailed() {
        verifyThat(userSession.getDriver().getPageSource(), containsString("Authentication Failed"));
    }
}
