package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.SideNavBarTab;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.search.Search;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public abstract class ActionFactory {
    private Application application;
    private ElementFactory elementFactory;

    public ActionFactory(Application application, ElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    protected Application getApplication() {
        return application;
    }

    protected WebDriver getDriver() {
        return elementFactory.getDriver();
    }

    protected ElementFactory getElementFactory() {
        return elementFactory;
    }

    protected void goToPage(NavBarTabId tab) {
        AppBody body = application.createAppBody(getDriver());
        body.getSideNavBar().switchPage(tab);
    }
}
