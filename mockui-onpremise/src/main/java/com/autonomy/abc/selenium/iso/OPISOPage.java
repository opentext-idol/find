package com.autonomy.abc.selenium.iso;

import com.autonomy.abc.selenium.analytics.OverviewPage;
import com.autonomy.abc.selenium.application.AppPageFactory;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.keywords.OPCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.OPKeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.promotions.OPCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.OPPromotionsPage;
import com.autonomy.abc.selenium.promotions.SchedulePage;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.OPSearchPage;
import com.autonomy.abc.selenium.users.OPLoginPage;
import com.autonomy.abc.selenium.users.OPUsersPage;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum OPISOPage implements PageMapper.Page, PageMapper.SwitchStrategy<SOElementFactory> {
    LOGIN(new OPLoginPage.Factory()),

    OVERVIEW(NavBarTabId.OVERVIEW, new OverviewPage.Factory()),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new OPPromotionsPage.Factory()),
    PROMOTION_WIZARD(new OPCreateNewPromotionsPage.Factory()),
    PROMOTION_DETAILS(new OPPromotionsDetailPage.Factory()),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory()),
    SCHEDULE(new SchedulePage.Factory()),

    KEYWORDS(NavBarTabId.KEYWORDS, new OPKeywordsPage.Factory()),
    KEYWORD_WIZARD(new OPCreateNewKeywordsPage.Factory()),

    SEARCH(NavBarTabId.SEARCH, new OPSearchPage.Factory()),

    ABOUT(OPISOTopNavBar.TabId.ABOUT, new AboutPage.Factory()),
    USERS(OPISOTopNavBar.TabId.USERS, new OPUsersPage.Factory()),
    SETTINGS(OPISOTopNavBar.TabId.SETTINGS, new SettingsPage.Factory());

    private PageMapper.SwitchStrategy<SOElementFactory> switchStrategy;
    private AppPageFactory<?> factory;

    <T extends AppPage> OPISOPage(AppPageFactory<T> factory) {
        this.factory = factory;
    }

    <T extends AppPage> OPISOPage(NavBarTabId tab, AppPageFactory<T> factory) {
        this(factory);
        switchStrategy = new OPISOElementFactory.SideNavStrategy(tab);
    }

    <T extends AppPage> OPISOPage(OPISOTopNavBar.TabId tab, AppPageFactory<T> factory) {
        this(factory);
        switchStrategy = new OPISOElementFactory.TopNavStrategy(tab);
    }

    @Override
    public Class<?> getPageType() {
        return factory.getPageType();
    }

    public Object loadAsObject(WebDriver driver) {
        return this.factory.create(driver);
    }


    @Override
    public void switchUsing(SOElementFactory context) {
        switchStrategy.switchUsing(context);
    }
}
