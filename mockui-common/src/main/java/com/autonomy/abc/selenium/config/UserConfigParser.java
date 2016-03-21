package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.users.AuthenticationStrategy;
import com.autonomy.abc.selenium.users.NewUser;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.util.Factory;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.WebDriver;

public interface UserConfigParser<T> {
    User parseUser(T userNode);
    NewUser parseNewUser(T newUserNode);
    NewUser generateNewUser(String identifier);
    AuthenticationStrategy getAuthenticationStrategy(Factory<WebDriver> driverFactory);
}
