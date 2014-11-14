package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class CreateNewPromotionsPage extends AppElement implements AppPage{


	public CreateNewPromotionsPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("promotions/create");
	}

	public WebElement pinToPosition() {
		return findElement(By.cssSelector("[data-promotion-type='PIN_TO_POSITION']"));
	}

	public WebElement continueButton(final String dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep + "']")).findElement(By.cssSelector(".next-step"));
	}

	public WebElement cancelButton(final String dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep + "']")).findElement(By.cssSelector(".cancel-wizard"));
	}

	public WebElement selectPositionMinusButton() {
		return findElement(By.cssSelector(".position-buttons-group .minus"));
	}

	public WebElement selectPositionPlusButton() {
		return findElement(By.cssSelector(".position-buttons-group .plus"));
	}

	public int positionInputValue() {
		return Integer.parseInt(findElement(By.cssSelector(".position")).getAttribute("value"));
	}

	public void addSearchTrigger(final String searchTrigger) {
		findElement(By.cssSelector(".promotion-trigger-input")).clear();
		findElement(By.cssSelector(".promotion-trigger-input")).sendKeys(searchTrigger);
		triggerAddButton().click();
	}

	public void removeSearchTrigger(String searchTrigger) {
		findElement(By.xpath(".//span[contains(text(), '" + searchTrigger + "')]/../i")).click();
	}

	public List<String> getSearchTriggersList() {
		final List<String> searchTriggerList = new ArrayList<>();

		for (final WebElement trigger : findElements(By.cssSelector(".trigger-word"))) {
			searchTriggerList.add(trigger.getText());
		}

		return searchTriggerList;
	}

	public WebElement triggerAddButton() {
		return findElement(By.cssSelector(".add-promotion-trigger [type='submit']"));
	}

	public WebElement promoteButton() {
		return findElement(By.cssSelector(".finish-step"));
	}

	public void navigateToTriggers() {
		pinToPosition().click();
		continueButton("type").click();
		modalLoadOrFadeWait();
		selectPositionPlusButton().click();
		continueButton("pinToPosition").click();
		modalLoadOrFadeWait();
	}

	public void typePositionNumber(final int positionNumber) {
		findElement(By.cssSelector(".position")).clear();
		findElement(By.cssSelector(".position")).sendKeys(String.valueOf(positionNumber));
	}

	public static class Placeholder {

		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public CreateNewPromotionsPage $createNewPromotionsPage(WebElement element) {
			return new CreateNewPromotionsPage(topNavBar, element);
		}
	}
}
