package com.autonomy.abc.selenium.predicates;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.regex.Pattern;

public class ElementTextMatchingRegex implements ExpectedCondition<String> {

	private final WebElement element;
	private final Pattern regex;

	public ElementTextMatchingRegex(final WebElement element, final Pattern regex) {
		this.element = element;
		this.regex = regex;
	}

	@Override
	public String apply(final WebDriver driver) {
		final String text = element.getText();
		if (text == null) return null;
		return regex.matcher(text).matches() ? text : null;
	}
}
