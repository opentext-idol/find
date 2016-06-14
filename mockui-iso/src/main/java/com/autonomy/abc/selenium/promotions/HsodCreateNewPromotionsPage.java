package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

public class HsodCreateNewPromotionsPage extends CreateNewPromotionsPage {
    private HsodCreateNewPromotionsPage(final WebDriver driver) {
        super(driver);
    }

    private FormInput dataInput(final String attribute) {
        return new FormInput(findElement(By.cssSelector("[data-attribute='" + attribute + "']")), getDriver());
    }

    @Override
    public List<WizardStep> getWizardSteps(final SpotlightPromotion promotion) {
        return Arrays.asList(
            new OptionWizardStep(this, "Promotion type", Promotion.Type.SPOTLIGHT.getOption()),
            new SearchTriggerStep(this, promotion.getTrigger())
        );
    }

    // for dynamic promotions
    public WebElement dial() {
        return findElement(By.cssSelector("input.dial"));
    }

    void setDialValue(final int value) {
        // .clear() does not work properly due to validation
        dial().sendKeys("\b\b" + Integer.toString(value));
    }

    @Override
    public List<WizardStep> getWizardSteps(final DynamicPromotion promotion) {
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

    public static class Factory extends SOPageFactory<HsodCreateNewPromotionsPage> {
        public Factory() {
            super(HsodCreateNewPromotionsPage.class);
        }

        @Override
        public HsodCreateNewPromotionsPage create(final WebDriver context) {
            return new HsodCreateNewPromotionsPage(context);
        }
    }
}
