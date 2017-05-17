package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.DashboardPage;
import com.autonomy.abc.selenium.find.OnPremNavBarSettings;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.login.IdolFindLoginPage;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.autonomy.abc.selenium.settings.SettingsPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.WebDriver;

public abstract class IdolFindElementFactory extends FindElementFactory {
    IdolFindElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public LoginPage getLoginPage() {
        return new IdolFindLoginPage(getDriver());
    }

    @Override
    public IdolFilterPanel getFilterPanel() {
        return new IdolFilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    @Override
    public OnPremNavBarSettings getTopNavBar() {
        return new OnPremNavBarSettings(getDriver());
    }

    public SettingsPage getSettingsPage() {
        return new SettingsPage.Factory().create(getDriver());
    }

    public DashboardPage getDashboard() {
        return new DashboardPage.Factory().create(getDriver());
    }
}
