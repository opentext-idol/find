package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SideNavBar extends AppElement {

    public SideNavBar(WebDriver driver){
        super(driver.findElement(By.cssSelector(".navbar-static-side")), driver);
    }

    public void switchPage(final NavBarTabId tab) {
        if (tab == null) {
            return;
        }

        if (!this.isDisplayed()) {
            toggle();
        }

        tab.click(getDriver());
    }

    public void toggle() {
        getDriver().findElement(By.className("navbar-minimize")).click();
    }
}
