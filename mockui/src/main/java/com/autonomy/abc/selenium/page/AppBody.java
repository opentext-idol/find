package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.WebDriver;

public abstract class AppBody {

    private final TopNavBar topNavBar;
    private final SideNavBar sideNavBar;
    protected final WebDriver driver;

    public AppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar) {
        this.driver = driver;
        this.topNavBar = topNavBar;
        this.sideNavBar = sideNavBar;
    }

    public TopNavBar getTopNavBar() { return topNavBar; }
    public SideNavBar getSideNavBar() { return sideNavBar; }

    public void navigateTo(String s){
        driver.get(s);
    }

    public abstract void logout();
}
