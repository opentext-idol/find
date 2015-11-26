package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.users.*;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

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
            newUser.signUpAs(Role.ADMIN, usersPage);
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
        userService.createNewUser(new HSONewUser("Valid", "hodtestqa401+NonInvalidEmail@gmail.com"), Role.ADMIN);

        usersPage.refreshButton().click();
        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));
    }

    @Test
    public void testAddedUserShowsUpAsPending(){
        HSONewUser newUser = new HSONewUser("VALIDUSER","hodtestqa401+NonInvalidEmail@gmail.com");

        HSOUser user = userService.createNewUser(newUser,Role.USER);

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));
        verifyThat(usersPage.getStatusOf(user), is(Status.PENDING));
        verifyThat(usersPage.getRoleOf(user), is(Role.USER));
    }

    @Test
    public void testResettingAuthentication(){
        HSONewUser newUser = new HSONewUser("authenticationtest","hodtestqa401+authenticationtest@gmail.com").validate();

        HSOUser user = userService.createNewUser(newUser,Role.USER);

        usersPage.refreshButton().click();
        verifyThat(usersPage.getStatusOf(user),is(Status.CONFIRMED));

        userService.resetAuthentication(user);

        user.resetAuthentication(usersPage);
    }

    private WebElement getContainingDiv(WebElement webElement){
        return webElement.findElement(By.xpath(".//../.."));
    }

    private WebElement getContainingDiv(FormInput formInput){
        return getContainingDiv(formInput.getElement());
    }
}
