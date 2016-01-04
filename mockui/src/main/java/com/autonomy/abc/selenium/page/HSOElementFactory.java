package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.devconsole.DevConsolePage;
import com.autonomy.abc.selenium.page.login.FindHasLoggedIn;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import com.autonomy.abc.selenium.page.promotions.*;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public class HSOElementFactory extends ElementFactory {
    public HSOElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public HSOPromotionsPage getPromotionsPage() {
        return new HSOPromotionsPage(getDriver());
    }

    @Override
    public LoginPage getLoginPage() {
        return new HSOLoginPage(getDriver(), new AbcHasLoggedIn(getDriver()));
    }

    public LoginPage getFindLoginPage() {
        return new HSOLoginPage(getDriver(), new FindHasLoggedIn(this));
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return new HSOKeywordsPage(getDriver());
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return new HSOCreateNewKeywordsPage(getDriver());
    }

    @Override
    public SearchPage getSearchPage() {
        return new HSOSearchPage(getDriver());
    }

    @Override
    public HSOCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return new HSOCreateNewPromotionsPage(getDriver());
    }

    public HSOUsersPage getUsersPage(){
        return new HSOUsersPage(getDriver());
    }

    public ConnectionsPage getConnectionsPage() {
        return ConnectionsPage.make(getDriver());
    }

    public NewConnectionPage getNewConnectionPage() {
        return NewConnectionPage.make(getDriver());
    }

    public ConnectionsDetailPage getConnectionsDetailPage() {
        return ConnectionsDetailPage.make(getDriver());
    }

    public Find getFindPage() {
        return new Find(getDriver());
    }


    public AnalyticsPage getAnalyticsPage() {
        return new AnalyticsPage(getDriver());
    }

    public IndexesPage getIndexesPage() {
        return new IndexesPage(getDriver());
    }

    public CreateNewIndexPage getCreateNewIndexPage() {
        return CreateNewIndexPage.make(getDriver());
    }

    public IndexesDetailPage getIndexesDetailPage() {
        return IndexesDetailPage.make(getDriver());
    }

    public GettingStartedPage getGettingStartedPage() {
        return new GettingStartedPage(getDriver());
    }

    public HSODevelopersPage getDevsPage() {
        return new HSODevelopersPage(getDriver());
    }

    public void waitForPage(NavBarTabId page) {
        switch(page){
            case ANALYTICS:
                getAnalyticsPage();
                break;
            case SEARCH:
                getSearchPage();
                break;
            case CONNECTIONS:
                getConnectionsPage();
                break;
            case INDEXES:
                getIndexesPage();
                break;
            case PROMOTIONS:
                getPromotionsPage();
                break;
            case KEYWORDS:
                getKeywordsPage();
                break;
            case GETTING_STARTED:
                getGettingStartedPage();
                break;
            case DEVELOPERS:
                getDevsPage();
                break;
            case USERS:
                getUsersPage();
                break;
        }
    }

    public DevConsolePage getDevConsolePage() {
        return new DevConsolePage(getDriver());
    }
}
