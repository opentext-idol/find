package com.autonomy.abc.selenium.menubar;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MainTab extends Tab {

	private WebElement link;

	public MainTab(final MainTabBar bar, final String pagename) {
		super(bar, By.cssSelector("ul li[data-pagename='" + pagename + "']"));
	}

	public MainTab(final WebElement $el, final WebDriver driver) {
		super($el, driver);
	}

	@Override
	public String getName() {
		return $el().findElement(By.tagName("a")).getText();
	}

	@Override
	public String retrieveId() {
		return $el().getAttribute("data-pagename");
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
