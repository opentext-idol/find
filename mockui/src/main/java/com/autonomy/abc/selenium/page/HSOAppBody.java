package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.HSO.HSOTopNavBar;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HSOAppBody extends AppBody {
    public HSOAppBody(WebDriver driver){
        this(driver, new HSOTopNavBar(driver), new SideNavBar(driver));
    }

    public HSOAppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar) {
        super(driver, topNavBar, sideNavBar);
    }

    @Override
    public void logout() {
        getTopNavBar().findElement(By.className("hp-settings")).click();
        getTopNavBar().findElement(By.className("navigation-logout")).click();
    }
}
