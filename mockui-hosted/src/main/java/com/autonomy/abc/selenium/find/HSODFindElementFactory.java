package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.navigation.ElementFactoryBase;
import com.autonomy.abc.selenium.navigation.PageMapper;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public class HSODFindElementFactory extends ElementFactoryBase {
    HSODFindElementFactory(WebDriver driver) {
        super(driver, new PageMapper<>(Page.class));
    }

    public LoginPage getLoginPage() {
        return loadPage(LoginPage.class);
    }

    public FindPage getFindPage() {
        return loadPage(FindPage.class);
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

        public Object loadAsObject(WebDriver driver) {
            return this.factory.create(driver);
        }
    }
}
