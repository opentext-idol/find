package com.autonomy.abc.selenium.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.regex.Pattern;

public class RegexMatchCondition implements ExpectedCondition<Boolean> {

	final WebElement element;
	final Pattern regex;

	public RegexMatchCondition(final WebElement element, final Pattern regex) {
		this.element = element;
		this.regex = regex;
	}

	@Override
	public Boolean apply(final WebDriver webDriver) {
		final String text = element.getText();
		return regex.matcher(text).matches();
	}

}
