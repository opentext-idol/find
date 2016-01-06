package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.Predicates;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PromotionsDetailPageTrigger extends Trigger {
    public PromotionsDetailPageTrigger(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    @Override
    public void addTrigger(String trigger) {
        super.addTrigger(trigger);
        waitForTriggerRefresh();
    }

    private void waitForTriggerRefresh() {
        new WebDriverWait(getDriver(), 20).until(Predicates.invisibilityOfAllElementsLocated(By.cssSelector(".term .fa-spin")));
    }
}
