package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.users.HSONewUser;
import com.autonomy.abc.selenium.users.HSOUserService;
import com.autonomy.abc.selenium.users.Role;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.not;
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
        usersPage.deleteOtherUsers();
    }

    @Test
    public void testCannotAddInvalidEmail(){
        HSONewUser newUser = new HSONewUser("jeremy","jeremy");

        usersPage.createUserButton().click();
        User user = newUser.signUpAs(Role.ADMIN, usersPage);

        verifyThat(getContainingDiv(usersPage.getUsernameInput()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.getEmailInput()), hasClass("has-error"));
        verifyThat(getContainingDiv(usersPage.getUserLevelDropdown()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.getUsernameInput()), not(hasClass("has-error")));

        usersPage.closeModal();

        usersPage.refreshButton().click();

        verifyThat(usersPage.getUsernames(), not(hasItem(user.getUsername())));
    }

    private WebElement getContainingDiv(WebElement webElement){
        return webElement.findElement(By.xpath(".//../.."));
    }

    private WebElement getContainingDiv(FormInput formInput){
        return getContainingDiv(formInput.getElement());
    }
}
