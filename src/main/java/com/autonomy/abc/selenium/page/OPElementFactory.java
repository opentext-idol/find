package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.page.admin.AboutPage;
import com.autonomy.abc.selenium.page.admin.SettingsPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.keywords.*;
import com.autonomy.abc.selenium.page.login.OPLoginPage;
import com.autonomy.abc.selenium.page.overview.OverviewPage;
import com.autonomy.abc.selenium.page.promotions.*;
import com.autonomy.abc.selenium.page.search.OPSearchPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public class OPElementFactory extends ElementFactory {
    // TODO: replace HSOs with OPs
    public OPElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public OPPromotionsPage getPromotionsPage() {
        return new OPPromotionsPage(getDriver());
    }

    @Override
    public OPLoginPage getLoginPage() {
        return new OPLoginPage(getDriver());
    }

    @Override
    public OPCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return new OPCreateNewPromotionsPage(getDriver());
    }

    @Override
    public OPKeywordsPage getKeywordsPage() {
        return new OPKeywordsPage(getDriver());
    }

    @Override
    public OPCreateNewKeywordsPage getCreateNewKeywordsPage() {
        return new OPCreateNewKeywordsPage(getDriver());
    }

    @Override
    public OPSearchPage getSearchPage() {
        return new OPSearchPage(getDriver());
    }

    public OverviewPage getOverviewPage() {
        return OverviewPage.make(getDriver());
    }

    public SchedulePage getSchedulePage() {
        return SchedulePage.make(getDriver());
    }

    public UsersPage getUsersPage() {
        return UsersPage.make(getDriver());
    }

    public AboutPage getAboutPage() {
        return AboutPage.make(getDriver());
    }

    public SettingsPage getSettingsPage() {
        return SettingsPage.make(getDriver());
    }

}
