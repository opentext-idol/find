package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class SideNavBar extends AppElement {

    public SideNavBar(WebDriver driver){
        super(driver.findElement(By.cssSelector(".navbar-static-side")), driver);
    }

    public Tab getSelectedTab(){
        final List<WebElement> activeTabs = $el().findElements(By.cssSelector("li.active"));

        if (activeTabs.size() != 1) {
            throw new IllegalStateException("Number of active tabs != 1");
        }

        return new SideNavBarTab(activeTabs.get(0), getDriver());
    }

    public void switchPage(final NavBarTabId tab) {
        if (!this.isDisplayed()) {
            toggle();
        }

        tab.click(getDriver());
    }

    public void toggle() {
        getDriver().findElement(By.className("navbar-minimize")).click();
    }
}
