package com.autonomy.abc.shared;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.users.UsersPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.TimeoutException;

import java.util.NoSuchElementException;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.modalIsDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.lift.Matchers.displayed;

public class UserTestHelper {
    private IsoApplication<?> app;
    private AuthenticationStrategy authStrategy;
    private UserService userService;
    private TestConfig config;
    private IsoElementFactory factory;

    public UserTestHelper(IsoApplication<?> app, TestConfig config) {
        this.app = app;
        this.authStrategy = config.getAuthenticationStrategy();
        this.userService = app.userService();
        this.config = config;
        this.factory = app.elementFactory();
    }

    public User singleSignUp(NewUser toCreate) {
        UsersPage usersPage = userService.getUsersPage();

        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final ModalView newUserModal = usersPage.userCreationModal();
        User user = usersPage.addNewUser(toCreate, Role.USER);
        authStrategy.authenticate(user);
//		assertThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        verifyUserAdded(newUserModal, user);
        newUserModal.close();
        return user;
    }


    public void signUpAndLoginAs(NewUser newUser, Window window) {
        UsersPage usersPage = userService.getUsersPage();

        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());

        User user = usersPage.addNewUser(newUser, Role.USER);
        authStrategy.authenticate(user);
        usersPage.userCreationModal().close();

        try {
            Waits.waitForGritterToClear();
        } catch (InterruptedException e) { /**/ }

        logoutAndNavigateToWebApp(window);

        try {
            app.loginService().login(user);
        } catch (TimeoutException | NoSuchElementException e) { /* Probably because of the sessions you're already logged in */ }

        factory.getPromotionsPage();
        assertThat(window, url(not(containsString("login"))));
    }

    public void deleteAndVerify(User user) {
        UsersPage usersPage = userService.getUsersPage();
        userService.deleteUser(user);

        if (!app.isHosted()) {
            verifyThat(usersPage, containsText("User " + user.getUsername() + " successfully deleted"));
        } else {
            factory.getTopNavBar().openNotifications();
            verifyThat(factory.getTopNavBar().getNotifications().notificationNumber(1), containsText("Deleted user " + user.getUsername()));
            factory.getTopNavBar().closeNotifications();
        }
    }

    public void verifyUserAdded(ModalView newUserModal, User user){
        if (!app.isHosted()){
            verifyThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        }

        //Hosted notifications are dealt with within the sign up method and there is no real way to ensure that a user's been created at the moment
    }

    public void logoutAndNavigateToWebApp(Window window) {
        if (app.loginService().getCurrentUser() != null) {
            app.loginService().logout();
        }
        window.goTo(config.getAppUrl(app));
    }

    public void verifyCreateDeleteInTable(NewUser newUser) {
        User user = userService.createNewUser(newUser, Role.USER);
        String username = user.getUsername();

        UsersPage usersPage = userService.getUsersPage();
        verifyThat(usersPage.deleteButton(user), displayed());
        verifyThat(usersPage.getTable(), containsText(username));

        deleteAndVerify(user);
        verifyThat(usersPage.getTable(), not(containsText(username)));
    }

}
