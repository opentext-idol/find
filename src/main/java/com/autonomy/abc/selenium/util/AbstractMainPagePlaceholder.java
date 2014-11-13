package com.autonomy.abc.selenium.util;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.MainMenuItem;
import com.autonomy.abc.selenium.menubar.MainTabBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import org.openqa.selenium.By;

public abstract class AbstractMainPagePlaceholder<P extends AppElement> extends AbstractWebElementPlaceholder<P> {

	protected final MainTabBar tabBar;
	protected final TopNavBar topNavBar;
	private final String pageId;
	private final String tabId;
	private final boolean opensMenu;

	public AbstractMainPagePlaceholder(
			final AppBody body
			, final MainTabBar tabBar
			, final TopNavBar topNavBar
			, final String pageId
			, final String tabId
			, final boolean opensMenu
	) {
		super(body, By.cssSelector("[data-pagename='" + pageId + "']"));
		this.tabBar = tabBar;
		this.topNavBar = topNavBar;
		this.pageId = pageId;
		this.tabId = tabId;
		this.opensMenu = opensMenu;
	}

	@Override
	public void navigateToPage() {
		if (opensMenu) {
			tabBar.switchPage(tabId, pageId);
		} else {
			tabBar.switchPage(tabId);
		}
	}

	@Override
	public void navigateToDropDownPage() {
		topNavBar.switchPage(tabId);
	}

	@Override
	public boolean isDisplayed() {
		if (opensMenu) {
			return new MainMenuItem(tabBar.getTab(tabId), pageId).isSelected();
		} else {
			return tabBar.getTab(tabId).isSelected();
		}
	}
}
