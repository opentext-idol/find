package com.autonomy.abc.users;

import com.autonomy.abc.base.IsoHsodTestBase;
import com.autonomy.abc.fixtures.EmailTearDownStrategy;
import com.autonomy.abc.fixtures.UserTearDownStrategy;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.autonomy.abc.selenium.hsod.IsoHsodApplication;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordGroup;
import com.autonomy.abc.selenium.keywords.KeywordWizardType;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HsodPromotionsPage;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.users.UsersPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
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

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

/**
 * These tests could in theory be made to work on-prem, but the
 * behaviour on-prem is not "correct" - and probably not needed
 */
@RelatedTo("HOD-532")
public class UserPermissionsITCase extends IsoHsodTestBase {
    private UserService userService;

    private User user;
    private AuthenticationStrategy authStrategy;

    private Session userSession;

    private IsoApplication<?> userApp;
    private IsoElementFactory userElementFactory;

    public UserPermissionsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        userService = getApplication().userService();
        authStrategy = getConfig().getAuthenticationStrategy();

        user = userService.createNewUser(getConfig().getNewUser("james"), Role.ADMIN);
        authStrategy.authenticate(user);

        userApp = IsoApplication.ofType(getConfig().getType());
        userSession = launchInNewSession(userApp);
        userElementFactory = userApp.elementFactory();

        try {
            userApp.loginService().login(user);
        } catch (final NoSuchElementException e) {
            boolean assumeFailed = true;

            try {
                assumeThat("Authentication failed", userSession.getDriver().getPageSource(), not(containsString("Authentication Failed")));
                assumeThat("Promotions page not displayed", userApp.elementFactory().getPromotionsPage(), displayed());

                assumeFailed = false;
            } finally {
                if(assumeFailed) {
                    emailTearDown();
                        userTearDown();
                }
            }
        }
    }

    @After
    public void userTearDown() {
        new UserTearDownStrategy(getInitialUser()).tearDown(this);
    }

    @After
    public void emailTearDown() {
        new EmailTearDownStrategy(getMainSession(), authStrategy).tearDown(this);
    }

    @Test
    @ResolvedBug("HOD-532")
    @ActiveBug("HOD-1073")
    public void testCannotAddKeywords(){
        final List<String> blacklist = new ArrayList<>();
        blacklist.add("Dave");

        final KeywordsPage keywordsPage = userApp.keywordService().goToKeywords();

        keywordsPage.createNewKeywordsButton().click();
        final CreateNewKeywordsPage createNewKeywordsPage = userElementFactory.getCreateNewKeywordsPage();

        Waits.loadOrFadeWait();

        deleteUser();

        try {
            new KeywordGroup(KeywordWizardType.BLACKLIST, Language.ENGLISH, blacklist).makeWizard(createNewKeywordsPage).apply();

            verifyError();
        } catch (final TimeoutException e) {
            try {
                verifyError();
            } catch (final Exception f) {
                verifyAuthFailed();
            }
        } catch (final StaleElementReferenceException e) {
            Waits.loadOrFadeWait();
            verifyAuthFailed();
        }

        getApplication().switchTo(KeywordsPage.class);

        verifyThat(getElementFactory().getKeywordsPage().getBlacklistedTerms(), not(hasItem(blacklist.get(0))));
    }

    @Test
    @ResolvedBug("HOD-532")
    @ActiveBug("HOD-1073")
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
    @ResolvedBug("HOD-532")
    @ActiveBug("HOD-1073")
    public void testCannotAddStaticPromotion() {
        assumeThat(isHosted(), is(true));

        final IsoHsodApplication userApp = (IsoHsodApplication) this.userApp;

        final HsodPromotionsPage promotionsPage = userApp.promotionService().goToPromotions();

        promotionsPage.staticPromotionButton().click();
        final CreateNewPromotionsPage createNewPromotionsPage = userElementFactory.getCreateNewPromotionsPage();

        deleteUser();

        try {
            new StaticPromotion("TITLE", "CONTENT", "TRIGGER").makeWizard(createNewPromotionsPage).apply();

            verifyError();
        } catch (final StaleElementReferenceException e) {
            verifyAuthFailed();
        }
    }

    @Test
    @ResolvedBug("HOD-532")
    @ActiveBug("HOD-1073")
    public void testCannotAddUser(){
        final UsersPage usersPage = userApp.userService().goToUsers();

        deleteUser();

        try {
            usersPage.createUserButton().click();
            try {
                usersPage.addNewUser(getConfig().generateNewUser(), Role.ADMIN);
            } finally {
                usersPage.userCreationModal().close();
            }

           verifyError();
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            verifyAuthFailed();
        }
    }

    private void deleteUser() {
        userService.deleteUser(user);
    }

    private void verifyError(){
        final TopNavBar topNavBar = userElementFactory.getTopNavBar();
        topNavBar.openNotifications();
        final NotificationsDropDown notificationsDropDown = topNavBar.getNotifications();
        verifyThat(notificationsDropDown.getNotification(1).getMessage(), containsString("Error"));
    }

    private void verifyAuthFailed() {
        verifyThat(userSession.getDriver().getPageSource(), containsString("Authentication Failed"));
    }
}
