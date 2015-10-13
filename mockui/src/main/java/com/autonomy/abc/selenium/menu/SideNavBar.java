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

    public Tab getTab(NavBarTabId id){
        return null;
    }

    public Tab getSelectedTab(){
        final List<WebElement> activeTabs = $el().findElements(By.cssSelector("li.active"));

        if (activeTabs.size() != 1) {
            throw new IllegalStateException("Number of active tabs != 1");
        }

        return new SideNavBarTab(activeTabs.get(0), getDriver());
    }

    public String getPageName() {
        return getSelectedTab().getName();
    }

    public String getPageId() {
        return getSelectedTab().getId();
    }

    public void switchPage(final NavBarTabId tab) {
        tryClickThenTryParentClick(new WebDriverWait(getDriver(),30).until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul//li//*[text()=' " + tab.toString() + "']"))));
    }
}
