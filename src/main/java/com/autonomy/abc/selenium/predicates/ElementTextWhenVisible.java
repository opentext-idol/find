package com.autonomy.abc.selenium.predicates;

import org.openqa.selenium.WebElement;

import java.util.regex.Pattern;

/**
 * Returns the text inside an element, but only if it contains visible characters (not whitespace).
 */
public class ElementTextWhenVisible extends ElementTextMatchingRegex {

	public ElementTextWhenVisible(final WebElement element) {
		super(element, Pattern.compile(".*\\p{Graph}.*"));
	}
}
