package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.login.IdolFindLoginPage;
import com.autonomy.abc.selenium.find.save.SearchOptionsBar;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.WebDriver;

public class IdolFindElementFactory extends FindElementFactory {
    IdolFindElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public LoginPage getLoginPage() {
        return new IdolFindLoginPage(getDriver());
    }

    @Override
    public IdolFindPage getFindPage() {
        return new IdolFindPage.Factory().create(getDriver());
    }

    public SearchTabBar getSearchTabBar() {
        return new SearchTabBar(getDriver());
    }

    public SearchOptionsBar getSearchOptionsBar() {
        return new SearchOptionsBar(getDriver());
    }

    public DetailedPreviewPage getDetailedPreview(){
        return new DetailedPreviewPage.Factory().create(getDriver());
    }
}
