package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.IsoElementFactory;
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
import com.autonomy.abc.selenium.users.HsodDevelopersPage;
import com.autonomy.abc.selenium.users.HsodUsersPage;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public class IsoHsodElementFactory extends IsoElementFactory {
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
    public HsodUsersPage getUsersPage(){
        return loadPage(HsodUsersPage.class);
    }

    public HsodDevelopersPage getDevsPage() {
        return loadPage(HsodDevelopersPage.class);
    }

    protected static class SideNavStrategy extends IsoElementFactory.SideNavStrategy {
        protected SideNavStrategy(final NavBarTabId tabId) {
            super(tabId);
        }
    }
}
