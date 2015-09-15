package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class TabBar extends AppElement {

    public TabBar(final WebElement $el, final WebDriver driver) {
        super($el, driver);
    }

    public abstract Tab getTab(NavBarTabId id);

    public abstract Tab getSelectedTab();

    public String getPageName() {
        return getSelectedTab().getName();
    }

    public String getPageId() {
        return getSelectedTab().getId();
    }

}
