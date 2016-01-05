package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.SearchTriggerStep;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CreateNewPromotionsBase extends AppElement implements AppPage {

	public CreateNewPromotionsBase(final WebDriver driver) {
		super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
	}

	public String getCurrentStepTitle() {
		return findElement(By.cssSelector(".current-step-pill .current-step-title")).getText();
	}

	public WebElement continueButton() {
		return findElement(By.cssSelector(".wizard-controls .next-step"));
	}

	public WebElement finishButton() {
		return findElement(By.cssSelector(".wizard-controls .finish-step"));
	}

	public WebElement cancelButton() {
		return findElement(By.cssSelector(".wizard-controls .cancel-wizard"));
	}

	public WebElement previousButton(){
		return findElement(By.cssSelector(".wizard-controls .previous-step"));
	}

	public FormInput triggerBox() {
		return new FormInput(findElement(By.cssSelector("input[name='words']")), getDriver());
	}

	public void addSearchTrigger(final String searchTrigger) {
		triggerBox().setValue(searchTrigger);

		try {
			Waits.loadOrFadeWait();
			ElementUtil.waitUntilClickableThenClick(triggerAddButton(), getDriver());
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
			searchTriggerList.add(ElementUtil.getParent(trigger).getText());
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

	public WebElement spotlightType(final Promotion.SpotlightType type){
		return spotlightType(type.getOption());
	}

	@Deprecated
	public WebElement spotlightType(final String type ) {
		return ElementUtil.getParent(findElement(By.cssSelector("[data-option='" + type + "']")));
	}

	// "visited" by the promotion
	public List<WizardStep> getWizardSteps(DynamicPromotion promotion) {
		return Arrays.asList(
				new OptionWizardStep(this, "Spotlight type", promotion.getSpotlightType().getOption()),
				new SearchTriggerStep(this, promotion.getTrigger())
		);
	}

	public abstract List<WizardStep> getWizardSteps(SpotlightPromotion promotion);

	public WebElement promotionType(final String promotionType) {
		return ElementUtil.getParent(findElement(By.cssSelector("[data-option='" + promotionType + "']")));
	}

	@Override
	public void waitForLoad() {
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("pd-wizard")));
	}
}
