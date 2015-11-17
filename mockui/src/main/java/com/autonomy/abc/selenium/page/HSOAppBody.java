package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.HSO.HSOTopNavBar;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
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
        System.out.println("Cannot logout on hosted");
    }
}
