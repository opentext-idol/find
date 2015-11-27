package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.users.*;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
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
        verifyThat(getContainingDiv(usersPage.getEmailInput()), hasClass("has-error"));
        verifyThat(getContainingDiv(usersPage.getUserLevelDropdown()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.createButton()), not(hasClass("has-error")));

        usersPage.closeModal();

        usersPage.refreshButton().click();
        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));

        //TODO use own email addresses
        //Sometimes it requires us to add a valid user before invalid users show up
        userService.createNewUser(new HSONewUser("Valid", "hodtestqa401+NonInvalidEmail@gmail.com"), Role.ADMIN, config.getWebDriverFactory());

        usersPage.refreshButton().click();
        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));
    }

    @Test
    public void testAddedUserShowsUpAsPending(){
        HSONewUser newUser = new HSONewUser("VALIDUSER","hodtestqa401+NonInvalidEmail@gmail.com");

        HSOUser user = userService.createNewUser(newUser, Role.USER, config.getWebDriverFactory());

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));
        verifyThat(usersPage.getStatusOf(user), is(Status.PENDING));
        verifyThat(usersPage.getRoleOf(user), is(Role.USER));
    }

    @Test
    public void testResettingAuthentication(){
        HSONewUser newUser = new HSONewUser("authenticationtest","hodtestqa401+authenticationtest@gmail.com").validate();

        HSOUser user = userService.createNewUser(newUser,Role.USER, config.getWebDriverFactory());

        usersPage.refreshButton().click();
        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));

        userService.resetAuthentication(user);

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
        User user = userService.createNewUser(new HSONewUser("editUsername","hodtestqa401+editUsername@gmail.com"), Role.ADMIN, config.getWebDriverFactory());

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        userService.editUsername(user, "Dave");

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        try {
            userService.editUsername(user, "");
        } catch (TimeoutException e) { /* Should fail here as you're giving it an invalid username */ }

        verifyThat(usersPage.editUsernameInput(user).getElement().isDisplayed(),is(true));
        verifyThat(usersPage.editUsernameInput(user).getElement().findElement(By.xpath("./../..")), hasClass("has-error"));
    }

    private WebElement getContainingDiv(WebElement webElement){
        return webElement.findElement(By.xpath(".//../.."));
    }

    private WebElement getContainingDiv(FormInput formInput){
        return getContainingDiv(formInput.getElement());
    }
}
