package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.HSO.HSOTopNavBar;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OPAppBody extends AppBody {
    public OPAppBody(WebDriver driver){
        // TODO: this should be a OPTopNavBar or something
        this(driver, new HSOTopNavBar(driver), new SideNavBar(driver));
    }

    public OPAppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar) {
        super(driver, topNavBar, sideNavBar);
    }

    @Override
    public void logout() {
        getTopNavBar().findElement(By.cssSelector(".fa-cog")).click();
        getTopNavBar().findElement(By.xpath(".//a[text()=' Logout']")).click();
    }
}
