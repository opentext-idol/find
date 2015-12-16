package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.ErrorPage;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import com.autonomy.abc.selenium.page.login.FindHasLoggedIn;
import com.autonomy.abc.selenium.page.login.GoogleAuth;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Factory;
import com.autonomy.abc.topnavbar.on_prem_options.UsersPageTestBase;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;

public class UserManagementHostedITCase extends UsersPageTestBase {

    private HSOUserService userService;
    private HSOUsersPage usersPage;
    private final static Logger LOGGER = LoggerFactory.getLogger(UserManagementHostedITCase.class);
    private final SignupEmailHandler emailHandler;
    private final Factory<NewUser> newUserFactory;

    public UserManagementHostedITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        emailHandler = new GmailSignupEmailHandler((GoogleAuth) config.getUser("google").getAuthProvider());
        newUserFactory = config.getNewUserFactory();
    }

    @Before
    public void hostedSetUp(){
        userService = (HSOUserService) super.userService;
        usersPage = (HSOUsersPage) super.usersPage;
    }

    // CSA-1775
    // CSA-1800
    @Test
    public void testCannotAddInvalidEmail(){
        HSONewUser newUser = new HSONewUser("jeremy","jeremy");

        verifyAddingInvalidUser(newUser);
        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));

        //Sometimes it requires us to add a valid user before invalid users show up
        userService.createNewUser(new HSONewUser("Valid", gmailString("NonInvalidEmail")), Role.ADMIN);

        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));
    }

    // unlike on-prem, duplicate usernames (display names) are allowed
    @Test
    public void testDuplicateUsername() {
        User user = userService.createNewUser(aNewUser, Role.ADMIN);
        assertThat(usersPage.getUsernames(), hasSize(1));
        verifyAddingValidUser(new HSONewUser(user.getUsername(), gmailString("isValid")));
    }

    //CSA-1776
    //CSA-1800
    @Test
    public void testAddingValidDuplicateAfterInvalid() {
        final String username = "bob";
        verifyAddingInvalidUser(new HSONewUser(username, "INVALID_EMAIL"));
        verifyAddingValidUser(new HSONewUser(username, gmailString("isValid")));
        verifyAddingValidUser(new HSONewUser(username, gmailString("alsoValid")));
    }

    private void verifyAddingInvalidUser(HSONewUser invalidUser) {
        int existingUsers = usersPage.getUsernames().size();
        usersPage.createUserButton().click();

        try {
            invalidUser.signUpAs(Role.ADMIN, usersPage);
        } catch (TimeoutException | HSONewUser.UserNotCreatedException e){
            /* Expected behaviour */
        }

        verifyModalElements();
        verifyThat(ModalView.getVisibleModalView(getDriver()).getText(), containsString(Errors.User.CREATING));
        usersPage.closeModal();

        verifyThat("number of users has not increased", usersPage.getUsernames(), hasSize(existingUsers));

        usersPage.loadOrFadeWait();

        verifyThat("number of users has not increased after refresh", usersPage.getUsernames(), hasSize(existingUsers));
    }

    private HSOUser verifyAddingValidUser(HSONewUser validUser) {
        int existingUsers = usersPage.getUsernames().size();
        usersPage.createUserButton().click();

        HSOUser user = validUser.signUpAs(Role.ADMIN, usersPage);

        verifyModalElements();
        verifyThat(ModalView.getVisibleModalView(getDriver()).getText(), not(containsString(Errors.User.CREATING)));
        usersPage.closeModal();

        verifyThat(usersPage.getUsernames(), hasItem(validUser.getUsername()));

        usersPage.loadOrFadeWait();
        verifyThat("exactly one new user appears", usersPage.getUsernames(), hasSize(existingUsers + 1));
        return user;
    }

    private void verifyModalElements() {
        verifyModalElement(usersPage.getUsernameInput().getElement());
        verifyModalElement(usersPage.getEmailInput().getElement());
        verifyModalElement(usersPage.userLevelDropdown());
        verifyModalElement(usersPage.createButton());
    }

    private void verifyModalElement(WebElement input) {
        verifyThat(getContainingDiv(input), not(hasClass("has-error")));
    }

    @Test
    public void testResettingAuthentication(){
        NewUser newUser = newUserFactory.create();

        final HSOUser user = userService.createNewUser(newUser,Role.USER);
        user.authenticate(config.getWebDriverFactory(), emailHandler);

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
        user.authenticate(config.getWebDriverFactory(), emailHandler);
    }

    @Test
    public void testNoneUserConfirmation() {
        NewUser somebody = newUserFactory.create();
        User user = userService.createNewUser(somebody, Role.ADMIN);
        userService.changeRole(user, Role.NONE);
        verifyThat(usersPage.getStatusOf(user), is(Status.PENDING));

        user.authenticate(config.getWebDriverFactory(), emailHandler);
        waitForUserConfirmed(user);
        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));

        // TODO: use a single driver once 401 page has logout button
        WebDriver secondDriver = config.getWebDriverFactory().create();
        try {
            secondDriver.get(config.getWebappUrl());
            LoginPage loginPage = new HSOLoginPage(secondDriver, new AbcHasLoggedIn(secondDriver));
            loginTo(loginPage, secondDriver, user);
            ErrorPage errorPage = new ErrorPage(secondDriver);
            verifyThat(errorPage.getErrorCode(), is("401"));
        } finally {
            secondDriver.quit();
        }
    }

    @Test
    public void testEditingUsername(){
        User user = userService.createNewUser(new HSONewUser("editUsername", gmailString("editUsername")), Role.ADMIN);

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        userService.editUsername(user, "Dave");

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        try {
            userService.editUsername(user, "");
        } catch (TimeoutException e) { /* Should fail here as you're giving it an invalid username */ }

        verifyThat(usersPage.editUsernameInput(user).getElement().isDisplayed(), is(true));
        verifyThat(usersPage.editUsernameInput(user).getElement().findElement(By.xpath("./../..")), hasClass("has-error"));
    }

    @Test
    public void testAddingAndAuthenticatingUser(){
        final User user = userService.createNewUser(newUserFactory.create(), Role.USER);
        user.authenticate(config.getWebDriverFactory(), emailHandler);

        waitForUserConfirmed(user);
        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));
    }

    @Test
    public void testCreateUser(){
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
        verifyThat(newUserModal, hasTextThat(startsWith("Create New Users")));

        usersPage.createButton().click();
        verifyThat(newUserModal, containsText(Errors.User.BLANK_EMAIL));

        String username = "Andrew";

        usersPage.addUsername(username);
        usersPage.clearEmail();
        usersPage.createButton().click();
        verifyThat(newUserModal, containsText(Errors.User.BLANK_EMAIL));

        usersPage.getEmailInput().setValue("hodtestqa401+CreateUserTest@gmail.com");
        usersPage.selectRole(Role.USER);
        usersPage.createButton().click();
//        verifyThat(newUserModal, containsText("Done! User Andrew successfully created"));

        usersPage.closeModal();
        verifyThat(usersPage, not(containsText("Create New Users")));   //Not sure what this is meant to be doing? Verifying modal no longer open??

        //CSA-1766
        verifyThat(usersPage.getUsernames(),hasItem(username));
    }

    @Test
    public void testLogOutAndLogInWithNewUser() {
        final User user = userService.createNewUser(newUserFactory.create(), Role.ADMIN);
        user.authenticate(config.getWebDriverFactory(), emailHandler);

        logout();

        getDriver().get(config.getFindUrl());
        loginAs(user);

        if(!new FindHasLoggedIn((HSOElementFactory) getElementFactory()).hasLoggedIn()){
            fail("Haven't been logged in to find");
        }
    }

    @Test
    public void testAddStupidlyLongUsername() {
        final String longUsername = StringUtils.repeat("a", 100);

        User user = userService.createNewUser(new HSONewUser(longUsername, "hodtestqa401+longusername@gmail.com"), Role.ADMIN);
        assertThat(usersPage.getTable(), containsText(longUsername));
        userService.deleteUser(user);

        assertThat(usersPage.getTable(), not(containsText(longUsername)));
    }

    @Test
    public void testUserConfirmedWithoutRefreshing(){
        final User user = userService.createNewUser(config.getNewUserFactory().create(), Role.USER);
        user.authenticate(config.getWebDriverFactory(), emailHandler);

        new WebDriverWait(getDriver(), 30).pollingEvery(5,TimeUnit.SECONDS).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return usersPage.getStatusOf(user).equals(Status.CONFIRMED);
            }
        });
    }

    private void waitForUserConfirmed(User user){
        new WebDriverWait(getDriver(),60).pollingEvery(10, TimeUnit.SECONDS).withMessage("User not showing as confirmed").until(new WaitForUserToBeConfirmed(user));
        body = getBody();
    }

    private class WaitForUserToBeConfirmed implements ExpectedCondition<Boolean>{
        private final User user;

        WaitForUserToBeConfirmed(User user){
            this.user = user;
        }

        @Override
        public Boolean apply(WebDriver driver) {
            driver.navigate().refresh();
            usersPage = (HSOUsersPage) getElementFactory().getUsersPage();
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
