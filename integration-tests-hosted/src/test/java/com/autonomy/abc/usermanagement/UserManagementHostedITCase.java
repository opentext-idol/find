package com.autonomy.abc.usermanagement;

import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.base.IsoTearDown;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.error.ErrorPage;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.HsodFind;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.shared.UserTestHelper;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class UserManagementHostedITCase extends HostedTestBase {
    private final NewUser aNewUser;
    private final UserTestHelper helper;

    private HsodUserService userService;
    private HsodUsersPage usersPage;

    public UserManagementHostedITCase(TestConfig config) {
        super(config);
        aNewUser = config.getNewUser("james");
        helper = new UserTestHelper(getApplication(), config);
    }

    @Before
    public void setUp() {
        userService = getApplication().userService();
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
    }

    @After
    public void emailTearDown() {
        if (hasSetUp()) {
            helper.deleteEmails(getMainSession());
        }
    }

    @After
    public void userTearDown() {
        IsoTearDown.USERS.tearDown(this);
    }

    @Test
    @KnownBug({"CSA-1775", "CSA-1800"})
    public void testCannotAddInvalidEmail(){
        HsodNewUser newUser = new HsodNewUser("jeremy","jeremy");

        verifyAddingInvalidUser(newUser);
        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));

        //Sometimes it requires us to add a valid user before invalid users show up
        userService.createNewUser(new HsodNewUser("Valid", gmailString("NonInvalidEmail")), Role.ADMIN);

        Waits.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));
    }

    // unlike on-prem, duplicate usernames (display names) are allowed
    @Test
    public void testDuplicateUsername() {
        User user = userService.createNewUser(aNewUser, Role.ADMIN);
        assertThat(usersPage.getUsernames(), hasSize(1));
        verifyAddingValidUser(new HsodNewUser(user.getUsername(), gmailString("isValid")));
    }

    @Test
    @KnownBug({"CSA-1776", "CSA-1800"})
    public void testAddingValidDuplicateAfterInvalid() {
        final String username = "bob";
        verifyAddingInvalidUser(new HsodNewUser(username, "INVALID_EMAIL"));
        verifyAddingValidUser(new HsodNewUser(username, gmailString("isValid")));
        verifyAddingValidUser(new HsodNewUser(username, gmailString("alsoValid")));
    }

    private void verifyAddingInvalidUser(HsodNewUser invalidUser) {
        int existingUsers = usersPage.getUsernames().size();
        usersPage.createUserButton().click();

        try {
            usersPage.addNewUser(invalidUser, Role.ADMIN);
        } catch (TimeoutException | UserNotCreatedException e){
            /* Expected behaviour */
        }

        verifyModalElements();
        verifyThat(ModalView.getVisibleModalView(getDriver()).getText(), containsString(Errors.User.CREATING));
        usersPage.userCreationModal().close();

        verifyThat("number of users has not increased", usersPage.getUsernames(), hasSize(existingUsers));

        Waits.loadOrFadeWait();

        verifyThat("number of users has not increased after refresh", usersPage.getUsernames(), hasSize(existingUsers));
    }

    private HsodUser verifyAddingValidUser(HsodNewUser validUser) {
        int existingUsers = usersPage.getUsernames().size();
        usersPage.createUserButton().click();

        HsodUser user = usersPage.addNewUser(validUser, Role.ADMIN);

        verifyModalElements();
        verifyThat(ModalView.getVisibleModalView(getDriver()).getText(), not(containsString(Errors.User.CREATING)));
        usersPage.userCreationModal().close();

        verifyThat(usersPage.getUsernames(), hasItem(validUser.getUsername()));

        Waits.loadOrFadeWait();
        verifyThat("exactly one new user appears", usersPage.getUsernames(), hasSize(existingUsers + 1));
        return user;
    }

    private void verifyModalElements() {
        HsodUserCreationModal modal = usersPage.userCreationModal();
        verifyModalElement(modal.usernameInput().getElement());
        verifyModalElement(modal.emailInput().getElement());
        verifyModalElement(modal.roleDropdown());
        verifyModalElement(modal.createButton());
    }

    private void verifyModalElement(WebElement input) {
        verifyThat(getContainingDiv(input), not(hasClass("has-error")));
    }

    @Test
    public void testResettingAuthentication(){
        NewUser newUser = getConfig().generateNewUser();

        final User user = userService.createNewUser(newUser,Role.USER);
        getConfig().getAuthenticationStrategy().authenticate(user);


        waitForUserConfirmed(user);

        userService.resetAuthentication(user);

        verifyThat(usersPage.getText(), containsString("Done! A reset authentication email has been sent to " + user.getUsername()));

        new Thread(){
            @Override
            public void run() {
                new WebDriverWait(getDriver(),180)
                        .withMessage("User never reset their authentication")
                        .until(GritterNotice.notificationContaining("User " + user.getUsername() + " reset their authentication"));

                LOGGER.info("User reset their authentication notification shown");
            }
        }.start();
        getConfig().getAuthenticationStrategy().authenticate(user);
    }

    @Test
    public void testNoneUserConfirmation() {
        NewUser somebody = getConfig().generateNewUser();
        User user = userService.createNewUser(somebody, Role.ADMIN);
        userService.changeRole(user, Role.NONE);
        verifyThat(usersPage.getStatusOf(user), is(Status.PENDING));

        getConfig().getAuthenticationStrategy().authenticate(user);
        waitForUserConfirmed(user);
        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));

        // TODO: use a single driver once 401 page has logout button
        IsoApplication<?> secondApp = IsoApplication.ofType(getConfig().getType());
        Window secondWindow = launchInNewSession(secondApp).getActiveWindow();
        try {
            try {
                secondApp.loginService().login(user);
            } catch (NoSuchElementException e) {
                /* Happens when it's trying to log in for the second time */
            }

            WebDriver secondDriver = secondWindow.getSession().getDriver();

            verify401(secondDriver);

            secondWindow.goTo(getAppUrl().split("/searchoptimizer")[0]);
            verify401(secondDriver);

            secondWindow.goTo(getConfig().getAppUrl(new HsodFind()));
            Waits.loadOrFadeWait();
            verifyThat(secondDriver.findElement(By.className("error-body")), containsText("401"));
        } finally {
            secondWindow.close();
        }
    }

    private void verify401(WebDriver driver){
        ErrorPage errorPage = new ErrorPage(driver);
        verifyThat(errorPage.getErrorCode(), is("401"));
    }

    @Test
    public void testEditingUsername(){
        User user = userService.createNewUser(new HsodNewUser("editUsername", gmailString("editUsername")), Role.ADMIN);

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        userService.editUsername(user, "Dave");

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        try {
            userService.editUsername(user, "");
        } catch (TimeoutException e) { /* Should fail here as you're giving it an invalid username */ }

        verifyThat(usersPage.editUsernameInput(user).getElement(), displayed());
        verifyThat(usersPage.editUsernameInput(user).getElement().findElement(By.xpath("./../..")), hasClass("has-error"));
    }

    @Test
    public void testAddingAndAuthenticatingUser(){
        final User user = userService.createNewUser(getConfig().generateNewUser(), Role.USER);
        getConfig().getAuthenticationStrategy().authenticate(user);

        waitForUserConfirmed(user);
        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));
    }

    @Test
    @KnownBug("CSA-1766")
    @RelatedTo("CSA-1663")
    public void testCreateUser(){
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final HsodUserCreationModal newUserModal = usersPage.userCreationModal();
        verifyThat(newUserModal, hasTextThat(startsWith("Create New Users")));

        newUserModal.createButton().click();
        verifyThat(newUserModal, containsText(Errors.User.BLANK_EMAIL));

        String username = "Andrew";

        newUserModal.usernameInput().setValue(username);
        newUserModal.emailInput().clear();
        newUserModal.createButton().click();
        verifyThat(newUserModal, containsText(Errors.User.BLANK_EMAIL));

        newUserModal.emailInput().setValue("hodtestqa401+CreateUserTest@gmail.com");
        newUserModal.selectRole(Role.USER);
        newUserModal.createUser();
//        verifyThat(newUserModal, containsText("Done! User Andrew successfully created"));

        usersPage.userCreationModal().close();
        verifyThat(usersPage, not(containsText("Create New Users")));   //Not sure what this is meant to be doing? Verifying modal no longer open??

        verifyThat(usersPage.getUsernames(),hasItem(username));
    }

    @Test
    @KnownBug("HOD-532")
    public void testLogOutAndLogInWithNewUser() {
        final User user = userService.createNewUser(getConfig().generateNewUser(), Role.ADMIN);
        getConfig().getAuthenticationStrategy().authenticate(user);

        getApplication().loginService().logout();
        HsodFind findApp = new HsodFind();
        redirectTo(findApp);

        boolean success = true;
        try {
            findApp.loginService().login(user);
        } catch (Exception e) {
            success = false;
        }
        verifyThat("logged in", success);
        verifyThat("taken to Find", getDriver().getTitle(), containsString("Find"));
    }

    @Test
    public void testAddStupidlyLongUsername() {
        final String longUsername = StringUtils.repeat("a", 100);
        helper.verifyCreateDeleteInTable(new HsodNewUser(longUsername, "hodtestqa401+longusername@gmail.com"));
    }

    @Test
    @KnownBug("HOD-532")
    public void testUserConfirmedWithoutRefreshing(){
        final User user = userService.createNewUser(getConfig().generateNewUser(), Role.USER);
        getConfig().getAuthenticationStrategy().authenticate(user);

        new WebDriverWait(getDriver(), 30).pollingEvery(5,TimeUnit.SECONDS).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return usersPage.getStatusOf(user).equals(Status.CONFIRMED);
            }
        });
    }

    private void waitForUserConfirmed(User user){
        new WebDriverWait(getDriver(),30).pollingEvery(10, TimeUnit.SECONDS).withMessage("User not showing as confirmed").until(new WaitForUserToBeConfirmed(user));
    }

    private class WaitForUserToBeConfirmed implements ExpectedCondition<Boolean>{
        private final User user;

        WaitForUserToBeConfirmed(User user){
            this.user = user;
        }

        @Override
        public Boolean apply(WebDriver driver) {
            getWindow().refresh();
            usersPage = getElementFactory().getUsersPage();
            Waits.loadOrFadeWait();
            return usersPage.getStatusOf(user).equals(Status.CONFIRMED);
        }
    }

    private WebElement getContainingDiv(WebElement webElement){
        return ElementUtil.ancestor(webElement, 2);
    }

    public static String gmailString(String plus){
        return "hodtestqa401+" + plus + "@gmail.com";
    }
}
