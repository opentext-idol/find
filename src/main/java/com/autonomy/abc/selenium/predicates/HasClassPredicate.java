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
public class HasClassPredicate implements Predicate<WebDriver> {

	private final String className;
	private final AppElement element;

	public HasClassPredicate(final AppElement element, final String className) {
		this.className = className;
		this.element = element;
	}

	@Override
	public boolean apply(final WebDriver driver) {
		return element.hasClass(className);
	}
}
