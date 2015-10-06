package com.autonomy.abc.selenium.page.promotions;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateNewPromotionsBase extends AppElement implements AppPage {

	public CreateNewPromotionsBase(final WebDriver driver) {
		super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
	}

	public WebElement continueButton(final WizardStep dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep.getTitle() + "']")).findElement(By.cssSelector(".next-step"));
	}

	public WebElement continueButton(){
		return findElement(By.cssSelector(".current-step .next-step"));
	}

	public String getCurrentStepTitle() {
		return findElement(By.cssSelector(".current-step-pill .current-step-title")).getText();
	}

	public enum WizardStep {
		TYPE("type"),
		PROMOTION_TYPE("promotionType"),
		RESULTS("results"),
		TRIGGER("triggers");


		private final String title;

		WizardStep(final String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

	public WebElement cancelButton(final WizardStep dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep.getTitle() + "']")).findElement(By.cssSelector(".cancel-wizard"));
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
		return findElement(By.cssSelector("[data-step='" + WizardStep.TRIGGER.getTitle() + "']")).findElement(By.xpath(".//button[contains(text(), 'Finish')]"));
	}

	public WebElement spotlightType(final String type ) {
		return getParent(findElement(By.cssSelector("[data-option='" + type + "']")));
	}

	/*
	public void addSpotlightPromotion(final String spotlightType, final String searchTrigger, final String type) {
		promotionType("SPOTLIGHT").click();
		loadOrFadeWait();
		continueButton(WizardStep.TYPE).click();
		loadOrFadeWait();
		if (type.equals("On Premise")) {
			spotlightType(spotlightType).click();
			loadOrFadeWait();
			continueButton(WizardStep.PROMOTION_TYPE).click();
			loadOrFadeWait();
		}
		addSearchTrigger(searchTrigger);
		loadOrFadeWait();
		finishButton().click();
		loadOrFadeWait();
	}*/

	public WebElement promotionType(final String promotionType) {
		return getParent(findElement(By.cssSelector("[data-option='" + promotionType + "']")));
	}

	@Override
	public void waitForLoad() {
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content")));
	}
}
