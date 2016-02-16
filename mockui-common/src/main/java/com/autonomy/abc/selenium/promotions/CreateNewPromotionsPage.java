package com.autonomy.abc.selenium.promotions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class CreateNewPromotionsPage extends CreateNewPromotionsBase {

	public CreateNewPromotionsPage(final WebDriver driver) {
		super(driver);
		waitForLoad();
	}

	public WebElement pinToPosition() {
		return findElement(By.cssSelector("[data-option='PIN_TO_POSITION']"));
	}

	public WebElement selectPositionMinusButton() {
		return findElement(By.cssSelector(".position-buttons-group .minus"));
	}

	public WebElement selectPositionPlusButton() {
		return findElement(By.cssSelector(".position-buttons-group .plus"));
	}

	public int positionInputValue() {
		return Integer.parseInt(findElement(By.cssSelector(".position")).getText());
	}

	public WebElement pinToPositionInput() {
		return findElement(By.cssSelector("div.position"));
	}

}
