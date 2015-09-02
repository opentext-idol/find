package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class AppBody {

    private final TopNavBar topNavBar;
    private final SideNavBar sideNavBar;
    private final WebDriver driver;
    private AppPage currentPage;

    public AppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar) {
        this.driver = driver;
        this.topNavBar = topNavBar;
        this.sideNavBar = sideNavBar;
    }

    public TopNavBar getTopNavBar() { return topNavBar; }
    public SideNavBar getSideNavBar() { return sideNavBar; }
    public AppPage getCurrentPage() { return currentPage; }

    public void navigateTo(AppPage page){
        page.navigateToPage(driver);
    }
}
