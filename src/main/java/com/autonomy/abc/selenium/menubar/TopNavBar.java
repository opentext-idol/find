package com.autonomy.abc.selenium.menubar;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class TopNavBar extends TabBar {

	public TopNavBar(final WebDriver driver) {
		super(driver.findElement(By.cssSelector(".navbar-static-top")), driver);
	}

	@Override
	public TopNavBarTab getTab(final NavBarTabId id) {
		return new TopNavBarTab(this, id.toString());
	}

	@Override
	public void switchPage(final NavBarTabId tabId) {
		findElement(By.cssSelector(".fa-cog")).click();
		super.switchPage(tabId);
	}

	@Override
	public TopNavBarTab getSelectedTab() {
		final List<WebElement> activeTabs = $el().findElements(By.cssSelector("li.active"));

		if (activeTabs.size() != 1) {
			throw new IllegalStateException("Number of active tabs != 1");
		}

		return new TopNavBarTab(activeTabs.get(0), getDriver());
	}

	public void search(final String searchTerm) {
		findElement(By.cssSelector("[name='top-search']")).clear();
		findElement(By.cssSelector("[name='top-search']")).sendKeys(searchTerm);
		findElement(By.cssSelector("[name='top-search']")).sendKeys(Keys.RETURN);
		loadOrFadeWait();
	}

	public void sideBarToggle() {
		findElement(By.cssSelector(".navbar-minimize")).click();
	}

	public void notificationsDropdown() {
		findElement(By.cssSelector(".fa-bell")).click();
	}
}