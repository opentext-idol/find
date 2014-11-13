package com.autonomy.abc.selenium.util;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.predicates.IsTrue;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class NoopPlaceholder<T extends AppElement> extends AbstractWebElementPlaceholder<T> {

	private final T element;

	public NoopPlaceholder(final T element) {
		super(element, null);
		this.element = element;
	}

	@Override
	public boolean hasElement() {
		return true;
	}

	@Override
	public T $el() {
		return element;
	}

	// NB: these things never go stale. This suits my current purposes, but a staleable option may also be useful.
	@Override
	public ExpectedCondition<Boolean> markAsStale() {
		return (element != null) ? ExpectedConditions.stalenessOf(element) : new IsTrue();
	}

	@Override
	protected T convertToActualType(final WebElement element) {
		throw new UnsupportedOperationException("You shouldn't need convertToActualType for NoopPlaceholder.");
	}
}
