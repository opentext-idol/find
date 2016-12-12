package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.login.FindHasLoggedIn;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FindHodFindElementFactory extends HodFindElementFactory {
    FindHodFindElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public FormInput getSearchBox() {
        return new FormInput(getDriver().findElement(By.cssSelector(".input-view-container .find-input")), getDriver());
    }

    @Override
    public LoginPage getLoginPage() {
        return new HSOLoginPage(getDriver(), new FindHasLoggedIn(getDriver()));
    }

}
