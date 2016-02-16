package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.menu.HSO.HSOTopNavBar;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.navigation.PageMapper;
import com.autonomy.abc.selenium.navigation.SOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
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
        return new HSOTopNavBar(getDriver());
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
        return loadPage(HSOKeywordsPage.class);
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadPage(HSOCreateNewKeywordsPage.class);
    }

    @Override
    public SearchPage getSearchPage() {
        return loadPage(HSOSearchPage.class);
    }

    @Override
    public HSODCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadPage(HSODCreateNewPromotionsPage.class);
    }

    @Override
    public HSOUsersPage getUsersPage(){
        return loadPage(HSOUsersPage.class);
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

    public HSODevelopersPage getDevsPage() {
        return loadPage(HSODevelopersPage.class);
    }

    protected static class SideNavStrategy extends SOElementFactory.SideNavStrategy {
        protected SideNavStrategy(NavBarTabId tabId) {
            super(tabId);
        }
    }
}
