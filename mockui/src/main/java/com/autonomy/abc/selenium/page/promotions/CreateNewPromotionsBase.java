package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

	@Deprecated
	public WebElement continueButton(final WizardStep dataStep) {
		return continueButton();
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

	private WebElement getVisibleElement(By by) {
		return new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOfElementLocated(By.className("current-step"))).findElement(by);
	}

	public WebElement continueButton() {
		return getVisibleElement(By.className("next-step"));
	}

	public WebElement finishButton() {
		return getVisibleElement(By.className("finish-step"));
	}

	public WebElement cancelButton() {
		return getVisibleElement(By.className("cancel-wizard"));
	}

	@Deprecated
	public WebElement cancelButton(final WizardStep dataStep) {
		return cancelButton();
	}

	public FormInput triggerBox() {
		return new FormInput(findElement(By.cssSelector("input[name='words']")), getDriver());
	}

	public void addSearchTrigger(final String searchTrigger) {
		triggerBox().setValue(searchTrigger);

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

	public String getTriggerError() {
		try {
			return findElement(By.className("help-block")).getText();
		} catch (NoSuchElementException e) {
			return null;
		}
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
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("pd-wizard")));
	}
}
