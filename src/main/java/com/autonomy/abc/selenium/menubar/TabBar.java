package com.autonomy.abc.selenium.menubar;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class TabBar extends AppElement {

	public TabBar(final WebElement $el, final WebDriver driver) {
		super($el, driver);
	}

	public abstract Tab getTab(NavBarTabId id);

	public abstract Tab getSelectedTab();

	public String getPageName() {
		return getSelectedTab().getName();
	}

	public String getPageId() {
		return getSelectedTab().getId();
	}

	public void switchPage(final NavBarTabId id) {
		final Tab tab = getTab(id);
		tab.click();
		loadOrFadeWait();
	}

}
