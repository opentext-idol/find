package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

public class HSODCreateNewPromotionsPage extends CreateNewPromotionsPage {
    private HSODCreateNewPromotionsPage(WebDriver driver) {
        super(driver);
    }

    private FormInput dataInput(String attribute) {
        return new FormInput(findElement(By.cssSelector("[data-attribute='" + attribute + "']")), getDriver());
    }

    @Override
    public List<WizardStep> getWizardSteps(SpotlightPromotion promotion) {
        return Arrays.asList(
            new OptionWizardStep(this, "Promotion type", Promotion.Type.SPOTLIGHT.getOption()),
            new SearchTriggerStep(this, promotion.getTrigger())
        );
    }

    // for dynamic promotions
    public WebElement dial() {
        return findElement(By.cssSelector("input.dial"));
    }

    void setDialValue(int value) {
        // .clear() does not work properly due to validation
        dial().sendKeys("\b\b" + Integer.toString(value));
    }

    @Override
    public List<WizardStep> getWizardSteps(DynamicPromotion promotion) {
        return Arrays.asList(
            new ResultsNumberStep(this, promotion.getNumberOfResults()),
            new SearchTriggerStep(this, promotion.getTrigger())
        );
    }

    // for static promotions
    public FormInput documentTitle() {
        return dataInput("staticTitle");
    }

    public FormInput documentContent() {
        return dataInput("staticContent");
    }

    public static class Factory implements ParametrizedFactory<WebDriver, HSODCreateNewPromotionsPage> {
        @Override
        public HSODCreateNewPromotionsPage create(WebDriver context) {
            return new HSODCreateNewPromotionsPage(context);
        }
    }
}
