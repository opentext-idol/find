package com.autonomy.abc.selenium.util;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.MainMenuItem;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import org.openqa.selenium.By;

public abstract class AbstractMainPagePlaceholder<P extends AppElement> extends AbstractWebElementPlaceholder<P> {

	protected final SideNavBar navBar;
	protected final TopNavBar topNavBar;
	private final String pageId;
	private final NavBarTabId tabId;
	private final boolean opensMenu;

	public AbstractMainPagePlaceholder(
			final AppBody body
			, final SideNavBar navBar
			, final TopNavBar topNavBar
			, final String pageId
			, final NavBarTabId tabId
			, final boolean opensMenu
	) {
		super(body, By.cssSelector("[data-pagename='" + pageId + "']"));
		this.navBar = navBar;
		this.topNavBar = topNavBar;
		this.pageId = pageId;
		this.tabId = tabId;
		this.opensMenu = opensMenu;
	}

	@Override
	public void navigateToPage() {
		if (opensMenu) {
			navBar.switchPage(tabId);
		} else {
			navBar.switchPage(tabId);
		}
	}

	@Override
	public void navigateToDropDownPage() {
		topNavBar.switchPage(tabId);
	}

	@Override
	public boolean isDisplayed() {
		if (opensMenu) {
			return new MainMenuItem(navBar.getTab(tabId), pageId).isSelected();
		} else {
			return navBar.getTab(tabId).isSelected();
		}
	}
}
