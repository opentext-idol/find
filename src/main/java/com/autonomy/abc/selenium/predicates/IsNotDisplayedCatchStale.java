package com.autonomy.abc.selenium.predicates;

import com.google.common.base.Predicate;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IsNotDisplayedCatchStale implements Predicate<WebDriver> {

	private final WebElement element;

	public IsNotDisplayedCatchStale(final WebElement element) {
		this.element = element;
	}

	@Override
	public boolean apply(final WebDriver driver) {

		try {
			return !element.isDisplayed();
		} catch (StaleElementReferenceException ex) {
			return true;
		}

	}
}
