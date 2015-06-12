package com.autonomy.abc.selenium.menubar;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SideNavBar extends TabBar {

	public SideNavBar(final WebDriver driver) {
		super(driver.findElement(By.cssSelector(".navbar-static-side")), driver);
	}

	@Override
	public SideNavBarTab getTab(final NavBarTabId id) {
		return new SideNavBarTab(this, id.toString());
	}

	@Override
	public void switchPage(final NavBarTabId tabId) {
		super.switchPage(tabId);
	}

	@Override
	public SideNavBarTab getSelectedTab() {
		final List<WebElement> activeTabs = $el().findElements(By.cssSelector("li.active"));

		if (activeTabs.size() != 1) {
			throw new IllegalStateException("Number of active tabs != 1");
		}

		return new SideNavBarTab(activeTabs.get(0), getDriver());
	}

}
