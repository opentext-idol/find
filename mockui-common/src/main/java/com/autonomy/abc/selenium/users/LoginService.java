package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.Application;
import com.autonomy.abc.selenium.navigation.ElementFactoryBase;

public class LoginService {
    private final ElementFactoryBase elementFactory;
    private User currentUser;

    public LoginService(Application<?> application) {
        elementFactory = application.elementFactory();
    }

    public void login(User user) {
        elementFactory.getLoginPage().loginWith(user.getAuthProvider());
        currentUser = user;
    }

    public void logout() {
        elementFactory.getLogoutHandler().logOut();
        currentUser = null;
    }

    /**
     * Get the currently logged in user
     * This can e.g. be compared with the username in the top bar
     * or used to send API calls using the user's API key
     * @return the user that the service expects to be logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    public interface LogoutHandler {
        void logOut();
    }
}
