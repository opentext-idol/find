package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HsodCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HsodKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.HsodCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HsodPromotionsPage;
import com.autonomy.abc.selenium.search.HsodSearchPage;
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
    public HsodPromotionsPage getPromotionsPage() {
        return loadPage(HsodPromotionsPage.class);
    }

    @Override
    public LoginPage getLoginPage() {
        return loadPage(HSOLoginPage.class);
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return loadPage(HsodKeywordsPage.class);
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadPage(HsodCreateNewKeywordsPage.class);
    }

    @Override
    public SearchPage getSearchPage() {
        return loadPage(HsodSearchPage.class);
    }

    @Override
    public HsodCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadPage(HsodCreateNewPromotionsPage.class);
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
