package com.autonomy.abc.selenium.predicates;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SubelementHasText implements Predicate<WebDriver> {

	private final WebElement element;
	private final By by;
	private final String text;

	public SubelementHasText(final WebElement element, final By by, final String text) {
		this.element = element;
		this.by = by;
		this.text = text;
	}

	@Override
	public boolean apply(final WebDriver driver) {
		final WebElement subelement = element.findElement(by);
		return text.equals(subelement.getText());
	}

}
