package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.users.*;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.NoSuchElementException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.modalIsDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

public class UsersPageTestBase extends ABCTestBase {
    public UsersPageTestBase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    protected final NewUser aNewUser = config.getNewUser("james");
    protected final NewUser newUser2 = config.getNewUser("john");
    protected UsersPage usersPage;
    protected UserService userService;
    protected int defaultNumberOfUsers = (getConfig().getType() == ApplicationType.HOSTED) ? 0 : 1;

    @Before
    public void setUp() throws MalformedURLException, InterruptedException {
        userService = getApplication().createUserService(getElementFactory());
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
    }

    protected User singleSignUp() {
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
        User user = aNewUser.signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
//		assertThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        verifyUserAdded(newUserModal, user);
        usersPage.closeModal();
        return user;
    }

    protected void signUpAndLoginAs(NewUser newUser) {
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());

        User user = newUser.signUpAs(Role.USER, usersPage, config.getWebDriverFactory());
        usersPage.closeModal();

        try {
            usersPage.waitForGritterToClear();
        } catch (InterruptedException e) { /**/ }

        logout();

        getDriver().get(getConfig().getWebappUrl());

        try {
            loginAs(user);
        } catch (TimeoutException | NoSuchElementException e) { /* Probably because of the sessions you're already logged in */ }

        getElementFactory().getPromotionsPage();
        assertThat(getDriver().getCurrentUrl(), not(containsString("login")));
    }

    protected void deleteAndVerify(User user) {
        userService.deleteUser(user);
        if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
            verifyThat(usersPage, containsText("User " + user.getUsername() + " successfully deleted"));
        } else {
            new WebDriverWait(getDriver(),10).withMessage("User " + user.getUsername() + " not successfully deleted").until(GritterNotice.notificationContaining("Deleted user " + user.getUsername()));
        }
    }

    protected void verifyUserAdded(ModalView newUserModal, User user){
        if(getConfig().getType().equals(ApplicationType.ON_PREM)){
            verifyThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        }

        //Hosted notifications are dealt with within the sign up method and there is no real way to ensure that a user's been created at the moment
    }

    protected void logoutAndNavigateToWebApp(){
        logout();
        getDriver().get(getConfig().getWebappUrl());
    }

    protected void verifyUserShowsUpInTable(User user, Status expectedStatus){
        verifyThat(usersPage.getUsernames(), CoreMatchers.hasItem(user.getUsername()));
        verifyThat(usersPage.getRoleOf(user), is(Role.USER));

        if(getConfig().getType().equals(ApplicationType.HOSTED)){
            HSOUsersPage usersPage = (HSOUsersPage) this.usersPage;
            HSOUser hsoUser = (HSOUser) user;

            verifyThat(usersPage.getEmailOf(user), is(hsoUser.getEmail()));
            verifyThat(usersPage.getStatusOf(user), is(expectedStatus));
        }
    }
}
