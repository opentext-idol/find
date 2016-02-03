package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.devconsole.DevConsoleHomePage;
import com.autonomy.abc.selenium.page.devconsole.DevConsoleSearchPage;
import com.autonomy.abc.selenium.page.login.DevConsoleHasLoggedIn;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public class DevConsoleElementFactory extends ElementFactoryBase {
    public DevConsoleElementFactory(WebDriver driver) {
        super(driver, new PageMapper<>(Page.class));
    }

    public LoginPage getDevConsoleLoginPage(){
        return loadPage(LoginPage.class);
    }

    public DevConsoleSearchPage getDevConsoleSearchPage() {
        return loadPage(DevConsoleSearchPage.class);
    }

    public DevConsoleHomePage getDevConsoleHomePage() {
        return loadPage(DevConsoleHomePage.class);
    }

    @Override
    protected void handleSwitch(NavBarTabId tab) {
        throw new UnsupportedOperationException("no tabs on dev console");
    }

    private enum Page implements PageMapper.Page {
        LOGIN(new ParametrizedFactory<WebDriver, HSOLoginPage>() {
            @Override
            public HSOLoginPage create(WebDriver context) {
                return new HSOLoginPage(context, new DevConsoleHasLoggedIn(context));
            }
        }, HSOLoginPage.class),
        HOME(new DevConsoleHomePage.Factory(), DevConsoleHomePage.class),
        SEARCH(new DevConsoleSearchPage.Factory(), DevConsoleSearchPage.class);

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
