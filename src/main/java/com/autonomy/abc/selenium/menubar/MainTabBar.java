package com.autonomy.abc.selenium.menubar;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class MainTabBar extends TabBar {

	public MainTabBar(final WebDriver driver) {
		super(driver.findElement(By.cssSelector("div.sidebar-collapse")), driver);
	}

	@Override
	public MainTab getTab(final String id) {
		return new MainTab(this, id);
	}

	@Override
	public void switchPage(final String tabId) {
		super.switchPage(tabId);
	}

	public void switchPage(final String tabId, final String menuItemId) {
		final MainTab tab = getTab(tabId);
		tab.click();
		final MainMenuItem menuItem = new MainMenuItem(tab, menuItemId);
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(menuItem));

		menuItem.click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.elementToBeSelected(menuItem));
	}


	@Override
	public MainTab getSelectedTab() {
		final List<WebElement> activeTabs = $el().findElements(By.cssSelector("li.active"));

		if (activeTabs.size() != 1) {
			throw new IllegalStateException("Number of active tabs != 1");
		}

		return new MainTab(activeTabs.get(0), getDriver());
	}

}
