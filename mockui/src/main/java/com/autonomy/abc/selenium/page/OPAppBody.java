package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.HSO.HSOTopNavBar;
import com.autonomy.abc.selenium.menu.OP.OPTopNavBar;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import org.openqa.selenium.WebDriver;

public class OPAppBody extends AppBody {
    public OPAppBody(WebDriver driver){
        this(driver, new OPTopNavBar(driver), new SideNavBar(driver));
    }

    public OPAppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar) {
        super(driver, topNavBar, sideNavBar);
    }

}
