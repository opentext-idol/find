package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.users.UsersPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public abstract class SOElementFactory extends ElementFactoryBase {
    private PageMapper<? extends PageMapper.SwitchStrategy<? super SOElementFactory>> switchMapper;

    protected SOElementFactory(WebDriver driver, PageMapper<? extends PageMapper.SwitchStrategy<? super SOElementFactory>> mapper) {
        super(driver, mapper);
        switchMapper = mapper;
    }

    public abstract TopNavBar getTopNavBar();

    public SideNavBar getSideNavBar() {
        return new SideNavBar(getDriver());
    }

    public abstract PromotionsPage getPromotionsPage();

    public abstract CreateNewPromotionsPage getCreateNewPromotionsPage();

    public PromotionsDetailPage getPromotionsDetailPage() {
        return loadPage(PromotionsDetailPage.class);
    }

    public EditDocumentReferencesPage getEditDocumentReferencesPage() {
        return loadPage(EditDocumentReferencesPage.class);
    }

    public abstract KeywordsPage getKeywordsPage();

    public abstract CreateNewKeywordsPage getCreateNewKeywordsPage();

    public abstract SearchPage getSearchPage();

    public abstract LoginPage getLoginPage();

    public abstract UsersPage getUsersPage();

    @Override
    public WebDriver getDriver() {
        return super.getDriver();
    }

    public void handleSwitch(Class<? extends AppPage> pageType) {
        switchMapper.get(pageType).switchUsing(this);
    }

    protected static class SideNavStrategy implements PageMapper.SwitchStrategy<SOElementFactory> {
        private final NavBarTabId tab;

        protected SideNavStrategy(NavBarTabId tabId) {
            tab = tabId;
        }

        @Override
        public void switchUsing(SOElementFactory context) {
            context.getSideNavBar().switchPage(tab);
        }
    }
}
