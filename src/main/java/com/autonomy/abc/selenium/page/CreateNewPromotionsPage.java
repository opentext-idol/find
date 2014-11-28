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

	public WebElement spotlight() {
		return findElement(By.cssSelector("[data-promotion-type='SPOTLIGHT']"));
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
		findElement(By.cssSelector("[name='terms']")).clear();
		findElement(By.cssSelector("[name='terms']")).sendKeys(searchTrigger);

		try {
			waitUntilClickableThenClick(triggerAddButton());
		} catch (final Exception e) {
			System.out.println("could not click trigger button with trigger " + searchTrigger);
		}
	}


	public void removeSearchTrigger(final String searchTrigger) {
		waitUntilClickableThenClick(By.xpath(".//span[contains(text(), '" + searchTrigger + "')]/i"));
	}

	public List<String> getSearchTriggersList() {
		final List<String> searchTriggerList = new ArrayList<>();

		for (final WebElement trigger : findElements(By.cssSelector(".remove-term"))) {
			searchTriggerList.add(getParent(trigger).getText());
		}

		return searchTriggerList;
	}

	public WebElement triggerAddButton() {
		return findElement(By.cssSelector(".term-input-form [type='submit']"));
	}

	public WebElement finishButton() {
		return findElement(By.cssSelector(".finish-step"));
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
		findElement(By.cssSelector(".position")).clear();
		findElement(By.cssSelector(".position")).sendKeys(String.valueOf(positionNumber));
	}

	public WebElement spotlightType(final String type ) {
		return findElement(By.cssSelector("[data-spotlight-type='" + type + "']"));
	}

	public String getTopPromotedLinkTitle() {
		return findElement(By.cssSelector(".promotions .search-result h3 a")).getText();
	}

	public String getTopPromotedLinkButtonText() {
		return findElement(By.cssSelector(".promotions .search-result button")).getText();
	}

	public void addSpotlightPromotion(final String spotlightType, final String searchTrigger) {
		spotlight().click();
		continueButton("type").click();
		spotlightType(spotlightType).click();
		continueButton("spotlightType").click();
		addSearchTrigger(searchTrigger);
		finishButton().click();
		loadOrFadeWait();
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
