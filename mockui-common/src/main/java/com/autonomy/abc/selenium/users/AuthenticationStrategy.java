package com.autonomy.abc.selenium.users;

import org.openqa.selenium.WebDriver;

public interface AuthenticationStrategy {
    void authenticate(User user);
    void cleanUp(WebDriver driver);
}
