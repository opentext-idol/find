package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public class HsodFindElementFactory extends FindElementFactory {
    HsodFindElementFactory(WebDriver driver) {
        super(driver, null);
    }

    @Override
    public LoginPage getLoginPage() {
        return new HSOLoginPage(getDriver(), new FindHasLoggedIn(getDriver()));
    }

    @Override
    public FindTopNavBar getTopNavBar() {
        return new FindTopNavBar(getDriver());
    }

    @Override
    public LoginService.LogoutHandler getLogoutHandler() {
        return getTopNavBar();
    }

    @Override
    public FindPage getFindPage() {
        return new FindPage.Factory().create(getDriver());
    }

    @Override
    public FindResultsPage getResultsPage() {
        return getFindPage().getResultsPage();
    }

    @Override
    public SimilarDocumentsView getSimilarDocumentsView() {
        return new SimilarDocumentsView.Factory().create(getDriver());
    }

    @Override
    public <T extends AppPage> T loadPage(Class<T> type) {
        throw new UnsupportedOperationException("loadPage does not make sense for a single page application");
    }
}
