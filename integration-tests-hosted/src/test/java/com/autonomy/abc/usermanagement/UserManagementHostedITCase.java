package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.apache.xpath.operations.Bool;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class UserManagementHostedITCase extends HostedTestBase {

    private HSOUserService userService;
    private HSOUsersPage usersPage;

    public UserManagementHostedITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp() {
        userService = getApplication().createUserService(getElementFactory());
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
    }

    @Test
    public void testCannotAddInvalidEmail(){
        HSONewUser newUser = new HSONewUser("jeremy","jeremy");

        usersPage.createUserButton().click();

        try {
            newUser.signUpAs(Role.ADMIN, usersPage, config.getWebDriverFactory());
        } catch (TimeoutException | HSONewUser.UserNotCreatedException e){ /* Expected behaviour */ }

        verifyThat(getContainingDiv(usersPage.getUsernameInput()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.getEmailInput()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.getUserLevelDropdown()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.createButton()), not(hasClass("has-error")));

        verifyThat(ModalView.getVisibleModalView(getDriver()).getText(), containsString("Error! New user profile creation failed."));

        usersPage.closeModal();

        usersPage.refreshButton().click();
        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));

        //Sometimes it requires us to add a valid user before invalid users show up
        userService.createNewUser(new HSONewUser("Valid", gmailString("NonInvalidEmail")), Role.ADMIN, config.getWebDriverFactory());

        usersPage.refreshButton().click();
        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));
    }

    @Test
    public void testResettingAuthentication(){
        HSONewUser newUser = new HSONewUser("resettingauthenticationtest",gmailString("resetauthtest")).authenticate();

        HSOUser user = userService.createNewUser(newUser,Role.USER, config.getWebDriverFactory());

        waitForUserConfirmed(user);

        userService.resetAuthentication(user);

        verifyThat(usersPage.getText(), containsString("Done! A reset authentication email has been sent to " + user.getUsername()));

        WebDriver driver = config.createWebDriver();
        try {
            user.resetAuthentication(driver);
        } finally {
            for(String browserHandle : driver.getWindowHandles()){
                driver.switchTo().window(browserHandle);
                driver.close();
            }
        }
    }

    @Test
    public void testEditingUsername(){
        User user = userService.createNewUser(new HSONewUser("editUsername", gmailString("editUsername")), Role.ADMIN, config.getWebDriverFactory());

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        userService.editUsername(user, "Dave");

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        try {
            userService.editUsername(user, "");
        } catch (TimeoutException e) { /* Should fail here as you're giving it an invalid username */ }

        verifyThat(usersPage.editUsernameInput(user).getElement().isDisplayed(),is(true));
        verifyThat(usersPage.editUsernameInput(user).getElement().findElement(By.xpath("./../..")), hasClass("has-error"));
    }

    @Test
    public void testAddingAndAuthenticatingUser(){
        final User user = userService.createNewUser(new HSONewUser("authenticatetest", gmailString("authenticationtest")).authenticate(),
                Role.USER, config.getWebDriverFactory());

        waitForUserConfirmed(user);

        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));
    }

    private void waitForUserConfirmed(User user){
        new WebDriverWait(getDriver(),20).withMessage("User not showing as confirmed").until(new waitForUserToBeConfirmed(getDriver(), user));
    }

    private class waitForUserToBeConfirmed implements ExpectedCondition<Boolean>{

        private final WebDriver driver;
        private final User user;

        waitForUserToBeConfirmed(WebDriver driver, User user){
            this.driver = driver;
            this.user = user;
        }

        @Override
        public Boolean apply(WebDriver driver) {
            usersPage.refreshButton().click();
            return usersPage.getStatusOf(user).equals(Status.CONFIRMED);
        }
    }

    private WebElement getContainingDiv(WebElement webElement){
        return webElement.findElement(By.xpath(".//../.."));
    }

    private WebElement getContainingDiv(FormInput formInput){
        return getContainingDiv(formInput.getElement());
    }

    private String gmailString(String plus){
        return "hodtestqa401+" + plus + "@gmail.com";
    }
}
