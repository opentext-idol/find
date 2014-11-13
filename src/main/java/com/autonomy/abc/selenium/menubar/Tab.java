package com.autonomy.abc.selenium.menubar;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class Tab extends AppElement {

	private String id;

	protected Tab(final TabBar parent, final By by) {
		super(parent.findElement(by), parent.getDriver());
	}

	protected Tab(final WebElement $el, final WebDriver driver) {
		super($el, driver);
	}

	public String getName() {
		return $el().getText();
	}

	public String getId() {
		if (id == null) {
			id = retrieveId();
		}

		return id;
	}

	protected String retrieveId() {
		return $el().getAttribute("href");
	}

	// want to make this abstract so that people don't forget to override it.
	@Override
	public abstract boolean isSelected();

}
