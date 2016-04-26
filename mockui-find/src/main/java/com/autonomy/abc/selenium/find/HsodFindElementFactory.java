package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.login.FindHasLoggedIn;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public class HsodFindElementFactory extends FindElementFactory {
    HsodFindElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public LoginPage getLoginPage() {
        return new HSOLoginPage(getDriver(), new FindHasLoggedIn(getDriver()));
    }

}
