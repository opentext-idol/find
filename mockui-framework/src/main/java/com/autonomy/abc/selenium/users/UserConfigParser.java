package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

public interface UserConfigParser<T> {
    User parseUser(T userNode);
    NewUser parseNewUser(T newUserNode);
    NewUser generateNewUser(String identifier);
    AuthenticationStrategy getAuthenticationStrategy(Factory<WebDriver> driverFactory);
}
