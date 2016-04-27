package com.autonomy.abc.selenium.find.login;

import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.WebDriver;

public class IdolFindLoginPage extends LoginPage {
    public IdolFindLoginPage(WebDriver driver) {
        super(driver, new FindHasLoggedIn(driver));
    }

    @Override
    public void waitForLoad() {}
}
