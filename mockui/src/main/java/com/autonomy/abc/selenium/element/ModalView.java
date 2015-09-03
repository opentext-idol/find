package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.predicates.HasCssValuePredicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ModalView extends AppElement {

	protected ModalView(final WebElement $el, final WebDriver driver) {
		super($el, driver);
	}

	public static ModalView getVisibleModalView(final WebDriver driver) {
		final WebElement $el = new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal[aria-hidden='false']")));
		final ModalView view = new ModalView($el, driver);
		new WebDriverWait(driver, 10).until(new HasCssValuePredicate(view, "opacity", "1"));
		return view;
	}

	public void close() {
		findElement(By.cssSelector("button.close")).click();
	}

	public WebElement okButton() {
		return findElement(By.cssSelector(".okButton"));
	}

	public void clickFooterButtonByColour(final Button.Colour colour) {
		findElement(By.cssSelector(".modal-footer button." + colour.fullName)).click();
	}
}
