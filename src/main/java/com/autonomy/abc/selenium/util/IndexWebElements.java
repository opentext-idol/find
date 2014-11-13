package com.autonomy.abc.selenium.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;

// TODO: rewrite table classes to use this.
public class IndexWebElements {

	public static <T extends WebElement> Map<String, Set<T>> indexAll(final Collection<T> elements) {
		return indexAll(elements, null);
	}

	public static <T extends WebElement> Map<String, Set<T>> indexAll(final Collection<T> elements, final By by) {
		final Map<String, Set<T>> output = new HashMap<>();

		for (final T elem : elements) {
			final String key = (by == null ? elem : elem.findElement(by)).getText();
			Set<T> elems = output.get(key);

			if (elems == null) {
				elems = new HashSet<>();
				output.put(key, elems);
			}

			elems.add(elem);
		}

		return output;
	}

	public static <T extends WebElement> Map<String, T> indexUnique(final Collection<T> elements) {
		return indexUnique(elements, null);
	}

	public static <T extends WebElement> Map<String, T> indexUnique(final Collection<T> elements, final By by) {
		final Map<String, Set<T>> rawOutput = indexAll(elements, by);
		final Map<String, T> output = new HashMap<>();

		for (final String key : rawOutput.keySet()) {
			final Set<T> elems = rawOutput.get(key);
			if (elems.size() > 1) throw new IllegalArgumentException("key is not unique: " + key);
			output.put(key, elems.iterator().next());
		}

		return output;
	}
}
