package com.autonomy.abc.selenium.find.comparison;

import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.predicates.HasCssValuePredicate;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;

public class ComparisonModal extends ModalView {
    private ComparisonModal(WebElement $el, WebDriver driver) {
        super($el, driver);
    }

    public void select(String savedSearchName) {
        findElement(new Locator().havingClass("secondary-model-title").containingText(savedSearchName)).click();
    }

    public WebElement compareButton() {
        return findElement(By.className("button-primary"));
    }

    public void waitForComparisonToLoad() {
        new WebDriverWait(getDriver(), 60)
                .withMessage("waiting for comparison to be fetched")
                .until(stalenessOf(this));
    }

    public static ComparisonModal make(WebDriver driver) {
        ComparisonModal modal = new ComparisonModal(driver.findElement(By.className("modal")), driver);
        new WebDriverWait(driver, 10).until(new HasCssValuePredicate(modal, "opacity", "1"));
        return modal;
    }
}
