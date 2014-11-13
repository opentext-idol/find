package com.autonomy.abc.selenium.predicates;

import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/*
 * $Id:$
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
public class ElementHasText implements Predicate<WebDriver> {

	private final WebElement element;
	private final String initialValue;

	public ElementHasText(final WebElement element, final String initialValue) {
		this.element = element;
		this.initialValue = initialValue;
	}

	@Override
	public boolean apply(final WebDriver webDriver) {
		return element.getText().trim().equals(initialValue);
	}
}
