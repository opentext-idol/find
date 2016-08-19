package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

public abstract class CreateNewPromotionsBase extends SOPageBase {

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

	public WebElement spotlightType(final Promotion.SpotlightType type){
		return ElementUtil.getParent(findElement(By.cssSelector("[data-option='" + type.getOption() + "']")));
	}

	// "visited" by the promotion
	public List<WizardStep> getWizardSteps(final DynamicPromotion promotion) {
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

	public TriggerForm getTriggerForm(){
		return new TriggerForm(findElement(By.className("trigger-words-form")), getDriver());
	}
}
