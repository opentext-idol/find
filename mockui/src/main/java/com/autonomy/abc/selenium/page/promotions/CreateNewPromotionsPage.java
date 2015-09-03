package com.autonomy.abc.selenium.page.promotions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CreateNewPromotionsPage extends CreateNewPromotionsBase {

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

	public void navigateToTriggers() {
		pinToPosition().click();
		continueButton(WizardStep.TYPE).click();
		loadOrFadeWait();
		selectPositionPlusButton().click();
		continueButton(WizardStep.PROMOTION_TYPE).click();
		loadOrFadeWait();
	}

	public void typePositionNumber(final int positionNumber) {
		pinToPositionInput().clear();
		pinToPositionInput().sendKeys(String.valueOf(positionNumber));
	}

	public WebElement pinToPositionInput() {
		return findElement(By.cssSelector("div.position"));
	}
}
