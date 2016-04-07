package com.autonomy.abc.selenium.iso;

import com.autonomy.abc.selenium.analytics.IsoOverviewPage;
import com.autonomy.abc.selenium.application.AppPageFactory;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.keywords.IdolCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.IdolKeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.promotions.IdolCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.IdolPromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.IdolPromotionsPage;
import com.autonomy.abc.selenium.promotions.SchedulePage;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.IdolIsoSearchPage;
import com.autonomy.abc.selenium.users.IdolIsoLoginPage;
import com.autonomy.abc.selenium.users.IdolUsersPage;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum IdolIsoPage implements PageMapper.Page, PageMapper.SwitchStrategy<SOElementFactory> {
    LOGIN(new IdolIsoLoginPage.Factory()),

    OVERVIEW(NavBarTabId.OVERVIEW, new IsoOverviewPage.Factory()),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new IdolPromotionsPage.Factory()),
    PROMOTION_WIZARD(new IdolCreateNewPromotionsPage.Factory()),
    PROMOTION_DETAILS(new IdolPromotionsDetailPage.Factory()),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory()),
    SCHEDULE(new SchedulePage.Factory()),

    KEYWORDS(NavBarTabId.KEYWORDS, new IdolKeywordsPage.Factory()),
    KEYWORD_WIZARD(new IdolCreateNewKeywordsPage.Factory()),

    SEARCH(NavBarTabId.SEARCH, new IdolIsoSearchPage.Factory()),

    ABOUT(IdolIsoTopNavBar.TabId.ABOUT, new IsoAboutPage.Factory()),
    USERS(IdolIsoTopNavBar.TabId.USERS, new IdolUsersPage.Factory()),
    SETTINGS(IdolIsoTopNavBar.TabId.SETTINGS, new IsoSettingsPage.Factory());

    private PageMapper.SwitchStrategy<SOElementFactory> switchStrategy;
    private AppPageFactory<?> factory;

    <T extends AppPage> IdolIsoPage(AppPageFactory<T> factory) {
        this.factory = factory;
    }

    <T extends AppPage> IdolIsoPage(NavBarTabId tab, AppPageFactory<T> factory) {
        this(factory);
        switchStrategy = new IdolIsoElementFactory.SideNavStrategy(tab);
    }

    <T extends AppPage> IdolIsoPage(IdolIsoTopNavBar.TabId tab, AppPageFactory<T> factory) {
        this(factory);
        switchStrategy = new IdolIsoElementFactory.TopNavStrategy(tab);
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
