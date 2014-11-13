package com.autonomy.abc.selenium.predicates;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class IsTrue implements ExpectedCondition<Boolean> {
	@Override
	public Boolean apply(final WebDriver driver) {
		return true;
	}
}
