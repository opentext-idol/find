package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateNewPromotionsBase extends AppElement implements AppPage {


	public CreateNewPromotionsBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("promotions/create");
	}

	public WebElement continueButton(final String dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep + "']")).findElement(By.cssSelector(".next-step"));
	}

	public WebElement cancelButton(final String dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep + "']")).findElement(By.cssSelector(".cancel-wizard"));
	}

	public void addSearchTrigger(final String searchTrigger) {
		findElement(By.cssSelector("input[name='words']")).clear();
		findElement(By.cssSelector("input[name='words']")).sendKeys(searchTrigger);

		try {
			loadOrFadeWait();
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

		for (final WebElement trigger : findElements(By.cssSelector(".remove-word"))) {
			searchTriggerList.add(getParent(trigger).getText());
		}

		return searchTriggerList;
	}

	public WebElement triggerAddButton() {
		return findElement(By.cssSelector(".trigger-words-form")).findElement(By.xpath(".//i[contains(@class, 'fa-plus')]/.."));
	}

	public WebElement finishButton() {
		return findElement(By.xpath(".//h3[contains(text(), 'Select Promotion Triggers')]/../../button[contains(text(), 'Finish')]"));
	}

	public WebElement spotlightType(final String type ) {
		return getParent(findElement(By.cssSelector("[data-option='" + type + "']")));
	}

	public void addSpotlightPromotion(final String spotlightType, final String searchTrigger, final String type) {
		promotionType("SPOTLIGHT").click();
		loadOrFadeWait();
		continueButton("type").click();
		loadOrFadeWait();
		if (type.equals("On Premise")) {
			spotlightType(spotlightType).click();
			loadOrFadeWait();
			continueButton("spotlightType").click();
			loadOrFadeWait();
		}
		addSearchTrigger(searchTrigger);
		loadOrFadeWait();
		finishButton().click();
		loadOrFadeWait();
	}

	public WebElement promotionType(final String promotionType) {
		return getParent(findElement(By.cssSelector("[data-option='" + promotionType + "']")));
	}

}
