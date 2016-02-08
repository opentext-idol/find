package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.menu.OPTopNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.admin.AboutPage;
import com.autonomy.abc.selenium.page.admin.SettingsPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.keywords.OPCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.OPKeywordsPage;
import com.autonomy.abc.selenium.page.login.OPLoginPage;
import com.autonomy.abc.selenium.page.overview.OverviewPage;
import com.autonomy.abc.selenium.page.promotions.OPCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.SchedulePage;
import com.autonomy.abc.selenium.page.search.OPSearchPage;
import com.autonomy.abc.selenium.users.OPUsersPage;
import org.openqa.selenium.WebDriver;

public class OPISOElementFactory extends SOElementFactory {
    public OPISOElementFactory(final WebDriver driver) {
        super(driver, new PageMapper<>(OPISOPage.class));
    }

    @Override
    public OPTopNavBar getTopNavBar() {
        return new OPTopNavBar(getDriver());
    }

    @Override
    public OPPromotionsPage getPromotionsPage() {
        return loadPage(OPPromotionsPage.class);
    }

    @Override
    public OPLoginPage getLoginPage() {
        return loadPage(OPLoginPage.class);
    }

    @Override
    public OPPromotionsDetailPage getPromotionsDetailPage() {
        return loadPage(OPPromotionsDetailPage.class);
    }

    @Override
    public OPCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadPage(OPCreateNewPromotionsPage.class);
    }

    @Override
    public OPKeywordsPage getKeywordsPage() {
        return loadPage(OPKeywordsPage.class);
    }

    @Override
    public OPCreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadPage(OPCreateNewKeywordsPage.class);
    }

    @Override
    public OPSearchPage getSearchPage() {
        return loadPage(OPSearchPage.class);
    }

    public SchedulePage getSchedulePage() {
        return loadPage(SchedulePage.class);
    }

    @Override
    public UsersPage getUsersPage() {
        return loadPage(OPUsersPage.class);
    }

    static class TopNavStrategy implements PageMapper.SwitchStrategy<SOElementFactory> {
        private final OPTopNavBar.TabId tab;

        TopNavStrategy(OPTopNavBar.TabId tabId) {
            tab = tabId;
        }

        @Override
        public void switchUsing(SOElementFactory context) {
            ((OPISOElementFactory) context).getTopNavBar().switchPage(tab);
        }
    }
}
