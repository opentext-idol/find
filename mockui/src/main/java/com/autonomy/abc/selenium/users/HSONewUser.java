package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSONewUser implements NewUser {

    private final String username;
    private final String email;
    private AuthProvider provider;

    public HSONewUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public HSONewUser(String username, String email, AuthProvider provider){
        this(username, email);
        this.provider = provider;
    }

    @Override
    public HSOUser signUpAs(Role role, UsersPage usersPage) {
        HSOUsersPage hsoUsersPage = (HSOUsersPage) usersPage;

        hsoUsersPage.addUsername(username);
        hsoUsersPage.addEmail(email);
        hsoUsersPage.selectRole(role);
        hsoUsersPage.createButton().click();

        new WebDriverWait(usersPage.getDriver(),15).withMessage("User hasn't been created").until(GritterNotice.notificationContaining("Created user"));

        Waits.loadOrFadeWait();

        if (hsoUsersPage.getUsernameInput().getValue().equals("")) {
            return new HSOUser(username, email, role, provider);
        }

        throw new UserNotCreatedException(this);
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        return null;
    }

    public class UserNotCreatedException extends RuntimeException {
        public UserNotCreatedException(HSONewUser user){
            this(user.username);
        }

        public UserNotCreatedException(String username){
            super("User '" + username + "' was not created");
        }
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return "NewUser<" + username + ":" + email + "|" + provider + ">";
    }
}
