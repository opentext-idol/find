package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.search.HSODSearchPage;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.users.HSODDevelopersPage;
import com.autonomy.abc.selenium.users.HSODUsersPage;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public class IsoHsodElementFactory extends SOElementFactory {
    IsoHsodElementFactory(final WebDriver driver) {
        super(driver, new PageMapper<>(IsoHsodPage.class));
    }

    @Override
    public TopNavBar getTopNavBar() {
        return new HsodTopNavBar(getDriver());
    }

    @Override
    public HSODPromotionsPage getPromotionsPage() {
        return loadPage(HSODPromotionsPage.class);
    }

    @Override
    public LoginPage getLoginPage() {
        return loadPage(HSOLoginPage.class);
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return loadPage(HSODKeywordsPage.class);
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadPage(HSODCreateNewKeywordsPage.class);
    }

    @Override
    public SearchPage getSearchPage() {
        return loadPage(HSODSearchPage.class);
    }

    @Override
    public HSODCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadPage(HSODCreateNewPromotionsPage.class);
    }

    @Override
    public HSODUsersPage getUsersPage(){
        return loadPage(HSODUsersPage.class);
    }

    public HSODDevelopersPage getDevsPage() {
        return loadPage(HSODDevelopersPage.class);
    }

    protected static class SideNavStrategy extends SOElementFactory.SideNavStrategy {
        protected SideNavStrategy(NavBarTabId tabId) {
            super(tabId);
        }
    }
}
