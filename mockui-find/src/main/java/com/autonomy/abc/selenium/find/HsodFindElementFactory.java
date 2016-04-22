package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class HsodFindElementFactory extends ElementFactoryBase {
    HsodFindElementFactory(WebDriver driver) {
        super(driver, new PageMapper<>(Page.class));
    }

    @Override
    public LoginPage getLoginPage() {
        return loadPage(LoginPage.class);
    }

    public FindTopNavBar getTopNavBar() {
        return new FindTopNavBar(getDriver());
    }

    @Override
    public LoginService.LogoutHandler getLogoutHandler() {
        return getTopNavBar();
    }

    public FindPage getFindPage() {
        return loadPage(FindPage.class);
    }

    public FindResultsPage getResultsPage() {
        return getFindPage().getResultsPage();
    }

    public SimilarDocumentsView getSimilarDocumentsView() {
        return loadPage(SimilarDocumentsView.class);
    }

    private enum Page implements PageMapper.Page {
        LOGIN(new ParametrizedFactory<WebDriver, HSOLoginPage>() {
            @Override
            public HSOLoginPage create(WebDriver context) {
                return new HSOLoginPage(context, new FindHasLoggedIn(context));
            }
        }, HSOLoginPage.class),
        MAIN(new FindPage.Factory(), FindPage.class),
        SIMILAR_DOCUMENTS(new SimilarDocumentsView.Factory(), SimilarDocumentsView.class);

        private final Class<? extends AppPage> pageType;
        private ParametrizedFactory<WebDriver, ? extends AppPage> factory;

        <T extends AppPage> Page(ParametrizedFactory<WebDriver, ? extends T> factory, Class<T> type) {
            pageType = type;
            this.factory = factory;
        }

        @Override
        public Class<?> getPageType() {
            return pageType;
        }

        public Object loadAsObject(WebDriver driver) {
            return this.factory.create(driver);
        }
    }
}
