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
	private final AbstractWebElementPlaceholder<UsersPage> usersPage;
	private final SearchPage.Placeholder searchPage;
	private final CreateNewPromotionsPage.Placeholder createPromotionsPage;
	private final TopNavBar topNavBar;

	public AppBody(final WebDriver driver) {
		this(driver, new MainTabBar(driver), new TopNavBar(driver));
	}

	public AppBody(final WebDriver driver, final MainTabBar mainTabBar, final TopNavBar topNavBar) {
		super(driver.findElement(By.cssSelector("body")), driver);

		this.overviewPage = new OverviewPage.Placeholder(this, mainTabBar, topNavBar);
		this.promotionsPage = new PromotionsPage.Placeholder(this, mainTabBar, topNavBar);
		this.aboutPage = new AboutPage.Placeholder(this, mainTabBar, topNavBar);
		this.usersPage = new UsersPage.Placeholder(this, mainTabBar, topNavBar);

		this.searchPage = new SearchPage.Placeholder(topNavBar);
		this.createPromotionsPage = new CreateNewPromotionsPage.Placeholder(topNavBar);
		this.topNavBar = new TopNavBar(driver);
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

	public UsersPage getUsersPage() {
		return usersPage.$topNavBarDropDownPage();
	}

	public SearchPage getSearchPage() {
		return searchPage.$searchPage(getDriver().findElement(By.cssSelector(".page-container")));
	}

	public CreateNewPromotionsPage getCreateNewPromotionsPage () {
		return createPromotionsPage.$createNewPromotionsPage(getDriver().findElement(By.cssSelector(".page-container")));
	}

	public TopNavBar getTopNavBar() {
		return topNavBar;
	}

	public void logout() {
		topNavBar.findElement(By.cssSelector(".fa-cog")).click();
		topNavBar.findElement(By.cssSelector("a[href='/abc/login/index.html']")).click();
	}
}
