package com.autonomy.abc.selenium.menubar;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MainMenuItem extends AppElement {

	private WebElement link;

	public MainMenuItem(final SideNavBarTab parent, final String id) {
		super(parent.findElement(By.cssSelector("[data-pagename='" + id + "']")), parent.getDriver());
	}

	public final WebElement getLink() {
		if (link == null) {
			link = findElement(By.tagName("a"));
		}

		return link;
	}

	@Override
	public void click() {
		getLink().click();
	}

	@Override
	public boolean isSelected() {
		return hasClass("active");
	}
}
