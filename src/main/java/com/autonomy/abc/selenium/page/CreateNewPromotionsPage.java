package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CreateNewPromotionsPage extends CreateNewPromotionsBase {

	public CreateNewPromotionsPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("promotions/create");
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
		continueButton("type").click();
		loadOrFadeWait();
		selectPositionPlusButton().click();
		continueButton("pinToPosition").click();
		loadOrFadeWait();
	}

	public void typePositionNumber(final int positionNumber) {
		pinToPositionInput().clear();
		pinToPositionInput().sendKeys(String.valueOf(positionNumber));
	}

	public WebElement pinToPositionInput() {
		return findElement(By.cssSelector("div.position"));
	}

	public static class Placeholder {

		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public CreateNewPromotionsPage $createNewPromotionsPage(final WebElement element) {
			return new CreateNewPromotionsPage(topNavBar, element);
		}
	}
}
