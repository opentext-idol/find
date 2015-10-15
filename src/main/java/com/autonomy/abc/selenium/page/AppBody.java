package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AppBody extends AppElement {

	private final TopNavBar topNavBar;
	private final SideNavBar sideNavBar;

	public AppBody(final WebDriver driver) {
		this(driver, new SideNavBar(driver), new TopNavBar(driver));
	}

	public AppBody(final WebDriver driver, final SideNavBar navBar, final TopNavBar topNavBar) {
		super(driver.findElement(By.cssSelector("body")), driver);

		this.topNavBar = new TopNavBar(driver);
		this.sideNavBar = new SideNavBar(driver);
	}

	public TopNavBar getTopNavBar() {
		return topNavBar;
	}

	public SideNavBar getSideNavBar() {
		return sideNavBar;
	}

	public void logout() {
		topNavBar.findElement(By.cssSelector(".fa-cog")).click();
		topNavBar.findElement(By.xpath(".//a[text()=' Logout']")).click();
	}

    @Deprecated
	public DatePicker getDatePicker() {
		return new DatePicker(this, getDriver());
	}
}
