package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.application.AppPageFactory;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.icma.GettingStartedPage;
import com.autonomy.abc.selenium.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.keywords.HSODCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODKeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.HSODSearchPage;
import com.autonomy.abc.selenium.users.HSODDevelopersPage;
import com.autonomy.abc.selenium.users.HSODUsersPage;
import com.autonomy.abc.selenium.users.SOHasLoggedIn;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum HSODPage implements PageMapper.Page, PageMapper.SwitchStrategy<SOElementFactory> {
    LOGIN(new AppPageFactory<HSOLoginPage>() {
        @Override
        public Class<HSOLoginPage> getPageType() {
            return HSOLoginPage.class;
        }

        @Override
        public HSOLoginPage create(WebDriver context) {
            return new HSOLoginPage(context, new SOHasLoggedIn(context));
        }
    }),

    ANALYTICS(NavBarTabId.ANALYTICS, new AnalyticsPage.Factory()),

    SEARCH(NavBarTabId.SEARCH, new HSODSearchPage.Factory()),

    CONNECTIONS(NavBarTabId.CONNECTIONS, new ConnectionsPage.Factory()),
    CONNECTION_WIZARD(new NewConnectionPage.Factory()),
    CONNECTION_DETAILS(new ConnectionsDetailPage.Factory()),

    INDEXES(NavBarTabId.INDEXES, new IndexesPage.Factory()),
    INDEX_WIZARD(new CreateNewIndexPage.Factory()),
    INDEX_DETAILS(new IndexesDetailPage.Factory()),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new HSODPromotionsPage.Factory()),
    PROMOTION_WIZARD(new HSODCreateNewPromotionsPage.Factory()),
    PROMOTION_DETAILS(new PromotionsDetailPage.Factory()),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory()),

    KEYWORDS(NavBarTabId.KEYWORDS, new HSODKeywordsPage.Factory()),
    KEYWORD_WIZARD(new HSODCreateNewKeywordsPage.Factory()),

    GETTING_STARTED(NavBarTabId.GETTING_STARTED, new GettingStartedPage.Factory()),

    DEVELOPERS(NavBarTabId.DEVELOPERS, new HSODDevelopersPage.Factory()),
    USERS(NavBarTabId.USERS, new HSODUsersPage.Factory());

    private PageMapper.SwitchStrategy<SOElementFactory> switchStrategy;
    private AppPageFactory<?> factory;

    <T extends AppPage> HSODPage(AppPageFactory<T> factory) {
        this.factory = factory;
    }

    <T extends AppPage> HSODPage(NavBarTabId tab, AppPageFactory<T> factory) {
        this(factory);
        this.switchStrategy = new HSODElementFactory.SideNavStrategy(tab);
    }

    @Override
    public Object loadAsObject(WebDriver driver) {
        return this.factory.create(driver);
    }

    @Override
    public Class<?> getPageType() {
        return factory.getPageType();
    }

    @Override
    public void switchUsing(SOElementFactory context) {
        switchStrategy.switchUsing(context);
    }
}
