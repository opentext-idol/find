package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.Predicates;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PromotionsDetailTriggerForm extends TriggerForm {
    public PromotionsDetailTriggerForm(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    @Override
    public void addTrigger(final String trigger) {
        super.addTrigger(trigger);
        waitForTriggerRefresh();
    }

    public void waitForTriggerRefresh() {
        new WebDriverWait(getDriver(), 20)
                .withMessage("refreshing triggers")
                .until(Predicates.invisibilityOfAllElementsLocated(By.cssSelector(".term .fa-spin")));
    }
}
