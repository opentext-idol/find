package com.autonomy.abc.selenium.predicates;

import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;

import java.util.regex.Pattern;

public class IsAtUrl implements Predicate<WebDriver> {

	private final Pattern pattern;

	public IsAtUrl(final String url, final boolean acceptExtension) {
		this.pattern = Pattern.compile("^" + Pattern.quote(url) + (acceptExtension ? ".*" : "$"));
	}

	public IsAtUrl(final Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean apply(final WebDriver driver) {
		final String url = driver.getCurrentUrl();
		return pattern.matcher(url).matches();
	}

}
