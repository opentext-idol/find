package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.navigation.PageMapper;
import com.autonomy.abc.selenium.navigation.SOElementFactory;
import com.autonomy.abc.selenium.users.HSODDevelopersPage;
import com.autonomy.abc.selenium.users.HSODUsersPage;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.search.HSODSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public class HSODElementFactory extends SOElementFactory {
    HSODElementFactory(final WebDriver driver) {
        super(driver, new PageMapper<>(HSODPage.class));
    }

    @Override
    public TopNavBar getTopNavBar() {
        return new HSODTopNavBar(getDriver());
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

    public ConnectionsPage getConnectionsPage() {
        return loadPage(ConnectionsPage.class);
    }

    public NewConnectionPage getNewConnectionPage() {
        return loadPage(NewConnectionPage.class);
    }

    public ConnectionsDetailPage getConnectionsDetailPage() {
        return loadPage(ConnectionsDetailPage.class);
    }

    public AnalyticsPage getAnalyticsPage() {
        return loadPage(AnalyticsPage.class);
    }

    public IndexesPage getIndexesPage() {
        return loadPage(IndexesPage.class);
    }

    public CreateNewIndexPage getCreateNewIndexPage() {
        return loadPage(CreateNewIndexPage.class);
    }

    public IndexesDetailPage getIndexesDetailPage() {
        return loadPage(IndexesDetailPage.class);
    }

    public GettingStartedPage getGettingStartedPage() {
        return loadPage(GettingStartedPage.class);
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
