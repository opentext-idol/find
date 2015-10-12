package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.promotions.DynamicPromotion;
import com.autonomy.abc.selenium.promotions.ResultsNumberStep;
import com.autonomy.abc.selenium.promotions.SearchTriggerStep;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.List;

public class HSOCreateNewPromotionsPage extends CreateNewPromotionsPage {
    public HSOCreateNewPromotionsPage(WebDriver driver) {
        super(driver);
    }

    private FormInput dataInput(String attribute) {
        return new FormInput(findElement(By.cssSelector("[data-attribute='" + attribute + "']")), getDriver());
    }

    // for dynamic promotions
    public FormInput dialInput() {
        return new FormInput(findElement(By.cssSelector("input.dial")), getDriver());
    }

    @Override
    public List<com.autonomy.abc.selenium.actions.wizard.WizardStep> getDynamicWizardSteps(DynamicPromotion promotion) {
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

    @Override
    public void addSpotlightPromotion(final String promotionType, final String searchTrigger) {
        // TODO: move this logic into a "SpotlightPromotion" object
        promotionType("SPOTLIGHT").click();
        continueButton().click();
        addSearchTrigger(searchTrigger);
        finishButton().click();
    }
}
