package com.autonomy.abc.selenium.menu;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SideNavBarTab extends Tab {
    private WebElement link;

    public SideNavBarTab(final SideNavBar bar, final String pagename) {
        super(bar, By.cssSelector("ul li[data-pagename='" + pagename + "']"));
    }

    public SideNavBarTab(final WebElement $el, final WebDriver driver) {
        super($el, driver);
    }

    @Override
    public String getName() {
        return $el().findElement(By.cssSelector(".nav-label")).getText();
    }

    @Override
    public String retrieveId() {
        return null;
    }

    @Override
    public boolean isSelected() {
        return this.hasClass("active");
    }

    @Override
    public void click() {
        getLink().click();
    }

    private WebElement getLink() {
        if (link == null) {
            link = findElement(By.tagName("a"));
        }

        return link;
    }
}
