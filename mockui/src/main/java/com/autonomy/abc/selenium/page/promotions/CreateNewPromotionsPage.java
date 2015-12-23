package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.util.Waits;
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

	@Deprecated
	public void navigateToTriggers() {
		pinToPosition().click();
		continueButton().click();
		Waits.loadOrFadeWait();
		selectPositionPlusButton().click();
		continueButton().click();
		Waits.loadOrFadeWait();
	}

	public void typePositionNumber(final int positionNumber) {
		pinToPositionInput().clear();
		pinToPositionInput().sendKeys(String.valueOf(positionNumber));
	}

	public WebElement pinToPositionInput() {
		return findElement(By.cssSelector("div.position"));
	}

    public abstract void addSpotlightPromotion(String promotionType, String searchTrigger);
}
