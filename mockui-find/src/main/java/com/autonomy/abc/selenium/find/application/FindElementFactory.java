package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.autonomy.abc.selenium.find.SimilarDocumentsView;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public abstract class FindElementFactory extends ElementFactoryBase {
    protected FindElementFactory(WebDriver driver) {
        super(driver, null);
    }

    @Override
    public LoginService.LogoutHandler getLogoutHandler() {
        return getTopNavBar();
    }

    public FindPage getFindPage() {
        return new FindPage.Factory().create(getDriver());
    }

    public FindTopNavBar getTopNavBar() {
        return new FindTopNavBar(getDriver());
    }

    public FindResultsPage getResultsPage() {
        return getFindPage().getResultsPage();
    }

    public SimilarDocumentsView getSimilarDocumentsView() {
        return new SimilarDocumentsView.Factory().create(getDriver());
    }

    @Override
    public <T extends AppPage> T loadPage(Class<T> type) {
        throw new UnsupportedOperationException("loadPage does not make sense for a single page application");
    }
}
