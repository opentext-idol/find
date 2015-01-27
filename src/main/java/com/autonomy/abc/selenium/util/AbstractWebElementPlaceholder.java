package com.autonomy.abc.selenium.util;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.predicates.IsTrue;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractWebElementPlaceholder<T extends WebElement> extends AppElement {

	private final SearchContext ancestor;
	private final By by;
	private T value;

	public AbstractWebElementPlaceholder(final AppElement ancestor, final By by) {
		this(ancestor.$el(), ancestor.getDriver(), by);
	}

	public AbstractWebElementPlaceholder(final SearchContext ancestor, final WebDriver driver, final By by) {
		super(null, driver);
		this.ancestor = ancestor;
		this.by = by;
	}

	public boolean hasElement() {
		return value != null;
	}

	public T $page() {
		if (!hasElement()) {
			navigateToPage();  // possibly should always do this. will navigate to page if it hasn't been seen yet - do we want it to do the same if it has?
			try {
				value = convertToActualType((WebElement) ancestor);
			} catch (final ClassCastException ex) {
				throw new IllegalArgumentException("attempting to apply a no-op placeholder to a SearchContext which is not a WebElement.", ex);
			}
		}
		return value;
	}

	public T $topNavBarDropDownPage() {
		if (!hasElement()) {
			navigateToDropDownPage();  // possibly should always do this. will navigate to page if it hasn't been seen yet - do we want it to do the same if it has?
			try {
				value = convertToActualType((WebElement) ancestor);
			} catch (final ClassCastException ex) {
				throw new IllegalArgumentException("attempting to apply a no-op placeholder to a SearchContext which is not a WebElement.", ex);
			}
		}
		return value;
	}

	//for use on elements on the page, allowing for a by to be passed, limiting the where the element can be located
	@Override
	public T $el() {
		if (!hasElement()) {
			navigateToPage();
			try {
				if (by == null) {
					value = convertToActualType((WebElement) ancestor);
				} else {
					value = convertToActualType(ancestor.findElement(by));
				}
			} catch (final ClassCastException ex) {
				throw new IllegalArgumentException("attempting to apply a no-op placeholder to a SearchContext which is not a WebElement.", ex);
			}
		}
		return value;
	}

	// idea: make this propagate to descendants
	public ExpectedCondition<Boolean> markAsStale() {
		final T old = value;
		value = null;
		return (old != null) ? ExpectedConditions.stalenessOf(old) : new IsTrue();
	}

	public void navigateToPage() {
	}

	public void navigateToDropDownPage() {
	}

	/**
	 * override to convert to type T
	 *
	 * @param element the element found using the By
	 * @return element converted to a T
	 */
	protected abstract T convertToActualType(WebElement element);

	@Override
	public String toString() {
		if (!hasElement()) {
			return "element is marked as stale";
		} else {
			try {
				return $el().getTagName() + " : " + $el().toString();
			} catch (final StaleElementReferenceException e) {
				return "element is stale, but is not marked as stale";
			}
		}
	}

}
