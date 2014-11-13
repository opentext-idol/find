package com.autonomy.abc.selenium.predicates;

import com.autonomy.abc.selenium.util.ImplicitWaits;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

public class ContainsSubelement implements Predicate<WebDriver> {

	private final SearchContext element;
	private final By by;

	public ContainsSubelement(final SearchContext element, final By by) {
		this.element = element;
		this.by = by;
	}

	@Override
	public boolean apply(final WebDriver driver) {
		ImplicitWaits.removeImplicitWait(driver);
		try {
			element.findElement(by);
			return true;
		} catch (NoSuchElementException ex) {
			return false;
		} finally {
			ImplicitWaits.setImplicitWait(driver);
		}
	}

}
