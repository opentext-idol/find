package com.autonomy.abc.usermanagement;

import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexWizard;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordGroup;
import com.autonomy.abc.selenium.keywords.KeywordWizardType;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
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
    private UserService userService;

    private User user;
    private AuthenticationStrategy authStrategy;
    private Session devSession;

    private Session userSession;

    private HSODApplication userApp;
    private HSODElementFactory userElementFactory;

    public UserPermissionsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        userService = getApplication().userService();
        authStrategy = getConfig().getAuthenticationStrategy();

        user = userService.createNewUser(getConfig().getNewUser("newhppassport"), Role.ADMIN);

        userApp = new HSODApplication();
        userSession = launchInNewSession(userApp);
        userElementFactory = userApp.elementFactory();

        authStrategy.authenticate(user);

        try {
            userApp.loginService().login(user);
        } catch (NoSuchElementException e) {
            boolean assumeFailed = true;

            try {
                assumeThat("Authentication failed", userSession.getDriver().getPageSource(), not(containsString("Authentication Failed")));
                assumeThat("Promotions page not displayed", userApp.elementFactory().getPromotionsPage(), displayed());

                assumeFailed = false;
            } finally {
                if(assumeFailed) {
                    tearDown();
                }
            }
        }
    }

    @After
    public void tearDown(){
        SOTearDown.USERS.tearDown(this);
        authStrategy.cleanUp(getDriver());
    }

    @Test
    public void testCannotNavigate(){
        verifyThat(userElementFactory.getPromotionsPage(), displayed());

        deleteUser();

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
        List<String> blacklist = new ArrayList<>();
        blacklist.add("Dave");

        KeywordsPage keywordsPage = userApp.keywordService().goToKeywords();

        keywordsPage.createNewKeywordsButton().click();
        CreateNewKeywordsPage createNewKeywordsPage = userElementFactory.getCreateNewKeywordsPage();

        Waits.loadOrFadeWait();

        deleteUser();

        try {
            new KeywordGroup(KeywordWizardType.BLACKLIST, Language.ENGLISH, blacklist).makeWizard(createNewKeywordsPage).apply();

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

        verifyThat(getElementFactory().getKeywordsPage().getBlacklistedTerms(), not(hasItem(blacklist.get(0))));
    }

    @Test
    public void testCannotAddPromotions(){
        userApp.switchTo(SearchPage.class);

        deleteUser();

        try {
            userApp.promotionService().setUpPromotion(new SpotlightPromotion("BE ALONE"), "Baggins", 2);

            verifyError();
        } catch (StaleElementReferenceException | TimeoutException e) {
            verifyThat(userSession.getDriver().getPageSource(), anyOf(containsString("Authentication Failed"), containsString("Error executing search action")));
        }
    }

    @Test
    public void testCannotAddStaticPromotion(){
        HSODPromotionsPage promotionsPage = userApp.promotionService().goToPromotions();

        promotionsPage.staticPromotionButton().click();
        HSODCreateNewPromotionsPage createNewPromotionsPage = userElementFactory.getCreateNewPromotionsPage();

        deleteUser();

        try {
            new StaticPromotion("TITLE", "CONTENT", "TRIGGER").makeWizard(createNewPromotionsPage).apply();

            verifyError();
        } catch (StaleElementReferenceException e) {
            verifyAuthFailed();
        }
    }

    @Test
    public void testCannotAddUser(){
        UsersPage usersPage = userApp.userService().goToUsers();

        deleteUser();

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

        deleteUser();

        try {
            new IndexWizard(index, createNewIndexPage).apply();

            verifyThat(getNotificationMessages(), hasItems("Index " + index.getDisplayName() + " has not been created", "Unauthorized"));
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

        deleteUser();

        try {
            connectionService.setUpConnection(connector);
        } catch (TimeoutException e) {
            verifyThat(getNotificationMessages(), hasItems("Failed to create a new connection: " + connectorName, "Failed to create '" + connectorName + "' connector", "Unauthorized"));
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

        deleteUser();

        userApp.switchTo(AnalyticsPage.class);

        List<String> notifications = getNotificationMessages();
        assertThat(notifications, hasItem("Unauthorized"));

        int unauthorized = 0;
        for(String notification : notifications){
            if(notification.contains("Unauthorized")){
                unauthorized++;
            }
        }

        verifyThat("One 'unauthorized' notification is shown", unauthorized, is(1));
    }

    private void deleteUser() {
        userService.deleteUser(user);
    }

    private List<String> getNotificationMessages(){
        TopNavBar topNavBar = userElementFactory.getTopNavBar();
        topNavBar.openNotifications();
        NotificationsDropDown notificationsDropDown = topNavBar.getNotifications();

        return notificationsDropDown.getAllNotificationMessages();
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
