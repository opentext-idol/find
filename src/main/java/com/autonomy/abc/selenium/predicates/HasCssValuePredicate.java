package com.autonomy.abc.selenium.predicates;

import com.autonomy.abc.selenium.AppElement;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
public class HasCssValuePredicate implements Predicate<WebDriver> {

	private final String cssProperty;
	private final String value;
	private final AppElement element;

	public HasCssValuePredicate(final AppElement element, final String cssProperty, final String value) {
		this.element = element;
		this.cssProperty = cssProperty;
		this.value = value;
	}

	@Override
	public boolean apply(final WebDriver driver) {
		final String cssValue = element.$el().getCssValue(cssProperty);
		return cssValue.equals(value);
	}
}
