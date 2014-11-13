package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.util.AbstractWebElementPlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Set;

public class Button extends AppElement {

	private Colour colour;

	public Button(final WebElement $el, final WebDriver driver) {
		super($el, driver);
		colour = null;
	}

	public void click() {
		$el().click();
	}

	public Colour getColour() {
		if (colour == null) {
			colour = retrieveColour();
		}
		return colour;
	}

	private Colour retrieveColour() {
		final Set<String> buttonClasses = AppElement.getClassSet($el());
		for (final Colour colour : Colour.values()) {
			if (buttonClasses.contains(colour.fullName)) {
				return colour;
			}
		}
		return Colour.DEFAULT;
	}

	public enum Colour {

		DEFAULT(),
		PRIMARY("primary"),
		INFO("info"),
		SUCCESS("success"),
		WARNING("warning"),
		DANGER("danger"),
		INVERSE("inverse"),
		LINK("link");

		public final String name;
		public final String fullName;

		private Colour(final String name) {
			this.name = name;
			this.fullName = "btn-" + name;
		}

		private Colour() {
			this.name = null;
			this.fullName = null;
		}

	}

	public static class Placeholder extends AbstractWebElementPlaceholder<Button> {
		public Placeholder(final AppElement ancestor, final By by) {
			super(ancestor, by);
		}

		@Override
		protected Button convertToActualType(final WebElement element) {
			return new Button(element, getDriver());
		}
	}
}
