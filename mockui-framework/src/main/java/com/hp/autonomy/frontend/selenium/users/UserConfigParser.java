package com.hp.autonomy.frontend.selenium.users;

import com.hp.autonomy.frontend.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

public interface UserConfigParser<T> {
    User parseUser(T userNode);
    NewUser parseNewUser(T newUserNode);
    NewUser generateNewUser(String identifier);
    AuthenticationStrategy getAuthenticationStrategy(Factory<WebDriver> driverFactory);
}
