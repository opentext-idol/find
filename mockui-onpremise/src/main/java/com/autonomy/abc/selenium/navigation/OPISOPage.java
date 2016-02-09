package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.OPTopNavBar;
import com.autonomy.abc.selenium.page.admin.AboutPage;
import com.autonomy.abc.selenium.page.admin.SettingsPage;
import com.autonomy.abc.selenium.page.keywords.OPCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.OPKeywordsPage;
import com.autonomy.abc.selenium.page.login.OPLoginPage;
import com.autonomy.abc.selenium.page.overview.OverviewPage;
import com.autonomy.abc.selenium.page.promotions.OPCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.SchedulePage;
import com.autonomy.abc.selenium.page.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.page.search.OPSearchPage;
import com.autonomy.abc.selenium.users.OPUsersPage;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum OPISOPage implements PageMapper.Page, PageMapper.SwitchStrategy<SOElementFactory> {
    LOGIN(new OPLoginPage.Factory(), OPLoginPage.class),

    OVERVIEW(NavBarTabId.OVERVIEW, new OverviewPage.Factory(), OverviewPage.class),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new OPPromotionsPage.Factory(), OPPromotionsPage.class),
    PROMOTION_WIZARD(new OPCreateNewPromotionsPage.Factory(), OPCreateNewPromotionsPage.class),
    PROMOTION_DETAILS(new OPPromotionsDetailPage.Factory(), OPPromotionsDetailPage.class),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory(), EditDocumentReferencesPage.class),
    SCHEDULE(new SchedulePage.Factory(), SchedulePage.class),

    KEYWORDS(NavBarTabId.KEYWORDS, new OPKeywordsPage.Factory(), OPKeywordsPage.class),
    KEYWORD_WIZARD(new OPCreateNewKeywordsPage.Factory(), OPCreateNewKeywordsPage.class),

    SEARCH(NavBarTabId.SEARCH, new OPSearchPage.Factory(), OPSearchPage.class),

    ABOUT(OPTopNavBar.TabId.ABOUT, new AboutPage.Factory(), AboutPage.class),
    USERS(OPTopNavBar.TabId.USERS, new OPUsersPage.Factory(), OPUsersPage.class),
    SETTINGS(OPTopNavBar.TabId.SETTINGS, new SettingsPage.Factory(), SettingsPage.class);

    private final Class<?> pageType;
    private PageMapper.SwitchStrategy<SOElementFactory> switchStrategy;
    private ParametrizedFactory<WebDriver, ?> factory;

    <T extends AppPage> OPISOPage(ParametrizedFactory<WebDriver, T> factory, Class<? super T> type) {
        pageType = type;
        this.factory = factory;
    }

    <T extends AppPage> OPISOPage(NavBarTabId tab, ParametrizedFactory<WebDriver, T> factory, Class<? super T> type) {
        this(factory, type);
        switchStrategy = new OPISOElementFactory.SideNavStrategy(tab);
    }

    <T extends AppPage> OPISOPage(OPTopNavBar.TabId tab, ParametrizedFactory<WebDriver, T> factory, Class<? super T> type) {
        this(factory, type);
        switchStrategy = new OPISOElementFactory.TopNavStrategy(tab);
    }

    @Override
    public Class<?> getPageType() {
        return pageType;
    }

    public Object loadAsObject(WebDriver driver) {
        return this.factory.create(driver);
    }


    @Override
    public void switchUsing(SOElementFactory context) {
        switchStrategy.switchUsing(context);
    }
}
