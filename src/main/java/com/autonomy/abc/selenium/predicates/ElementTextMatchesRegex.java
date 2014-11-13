package com.autonomy.abc.selenium.predicates;

import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.regex.Pattern;

public class ElementTextMatchesRegex implements Predicate<WebDriver> {

	private final WebElement element;
	private final Pattern regex;

	public ElementTextMatchesRegex(final WebElement element, final Pattern regex) {
		this.element = element;
		this.regex = regex;
	}

	@Override
	public boolean apply(final WebDriver driver) {
		final String text = element.getText();
		return regex.matcher(text).matches();
	}

}
