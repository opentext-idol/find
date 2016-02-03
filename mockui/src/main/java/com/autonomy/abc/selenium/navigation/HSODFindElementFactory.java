package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public class HSODFindElementFactory extends ElementFactoryBase {
    public HSODFindElementFactory(WebDriver driver) {
        super(driver, new PageMapper<>(Page.class));
    }

    public LoginPage getFindLoginPage() {
        return loadPage(LoginPage.class);
    }

    public FindPage getFindPage() {
        return loadPage(FindPage.class);
    }

    @Override
    protected void handleSwitch(NavBarTabId tab) {
        if (tab != null) {
            throw new UnsupportedOperationException("no tabs on Find");
        }
    }

    private enum Page implements PageMapper.Page {
        LOGIN(new ParametrizedFactory<WebDriver, HSOLoginPage>() {
            @Override
            public HSOLoginPage create(WebDriver context) {
                return new HSOLoginPage(context, new FindHasLoggedIn(context));
            }
        }, HSOLoginPage.class),
        MAIN(new FindPage.Factory(), FindPage.class);

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

        @Override
        public NavBarTabId getId() {
            return null;
        }

        @SuppressWarnings("unchecked")
        public <T extends AppPage> T safeLoad(Class<T> type, WebDriver driver) {
            if (type.isAssignableFrom(pageType)) {
                return (T) safeLoad(driver);
            }
            return null;
        }

        protected Object safeLoad(WebDriver driver) {
            return this.factory.create(driver);
        }
    }
}
