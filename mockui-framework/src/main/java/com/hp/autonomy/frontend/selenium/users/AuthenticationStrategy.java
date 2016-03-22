package com.hp.autonomy.frontend.selenium.users;

import org.openqa.selenium.WebDriver;

public interface AuthenticationStrategy {
    void authenticate(User user);
    void cleanUp(WebDriver driver);
}
