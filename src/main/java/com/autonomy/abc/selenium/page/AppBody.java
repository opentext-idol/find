package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.MainTabBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractWebElementPlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AppBody extends AppElement {

	private final AbstractWebElementPlaceholder<OverviewPage> overviewPage;
	private final AbstractWebElementPlaceholder<PromotionsPage> promotionsPage;
	private final AbstractWebElementPlaceholder<AboutPage> aboutPage;

	public AppBody(final WebDriver driver) {
		this(driver, new MainTabBar(driver), new TopNavBar(driver));
	}

	public AppBody(final WebDriver driver, final MainTabBar mainTabBar, final TopNavBar topNavBar) {
		super(driver.findElement(By.cssSelector("body")), driver);

		this.overviewPage = new OverviewPage.Placeholder(this, mainTabBar, topNavBar);
		this.promotionsPage = new PromotionsPage.Placeholder(this, mainTabBar, topNavBar);
		this.aboutPage = new AboutPage.Placeholder(this, mainTabBar, topNavBar);
	}

	public OverviewPage getOverviewPage() {
		return overviewPage.$page();
	}

	public PromotionsPage getPromotionsPage() {
		return promotionsPage.$page();
	}

	public AboutPage getAboutPage() {
		return aboutPage.$topNavBarDropDownPage();
	}
}
