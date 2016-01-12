package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.HSO.HSOTopNavBar;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

        Waits.loadOrFadeWait();

        getTopNavBar().findElement(By.className("navigation-logout")).click();

        new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.className("haven-splash-header")));
    }
}
