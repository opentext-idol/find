package com.autonomy.abc.selenium.promotions;

import com.hp.autonomy.frontend.selenium.element.*;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OPPromotionsDetailPage extends PromotionsDetailPage {
    private AppElement fieldTextContainer;

    private OPPromotionsDetailPage(WebDriver driver) {
        super(driver);
        fieldTextContainer = new AppElement(findElement(By.cssSelector(".promotion-field-text")), driver);
    }

    public WebElement fieldTextAddButton() {
        return fieldTextContainer.findElement(By.cssSelector("button[type=button]"));
    }

    public FormInput fieldTextInput() {
        return new FormInput(fieldTextContainer.findElement(By.cssSelector(".field-text-input")), getDriver());
    }

    public Editable editableFieldText() {
        return new InlineEdit(fieldTextContainer);
    }

    public Removable removableFieldText() {
        return new LabelBox(fieldTextContainer);
    }

    public void addFieldText(final String fieldText) {
        fieldTextAddButton().click();
        Waits.loadOrFadeWait();
        fieldTextInput().setAndSubmit(fieldText);
        waitForFieldTextToUpdate();
    }

    public void removeFieldText() {
        removableFieldText().removeAsync();
        waitForFieldTextToUpdate();
    }

    public void updateFieldText(final String newText) {
        editableFieldText().setValueAsync(newText);
        waitForFieldTextToUpdate();
    }

    public void closeInputBox() {
        fieldTextContainer.findElement(By.xpath("../h5")).click();
    }

    // must do this after changing field text directly
    public void waitForFieldTextToUpdate() {
        editableFieldText().waitForUpdate();
        // sleep since update spinner disappears once request has
        // finished sending, not once actually loaded
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getFieldTextError() {
        try {
            return fieldTextContainer.findElement(By.cssSelector(".field-text-error")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void schedulePromotion(){
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.elementToBeClickable(findElement(By.className("promotion-view-schedule")))).click();
    }

    public static class Factory extends SOPageFactory<OPPromotionsDetailPage> {
        public Factory() {
            super(OPPromotionsDetailPage.class);
        }

        public OPPromotionsDetailPage create(WebDriver context) {
            return new OPPromotionsDetailPage(context);
        }
    }
}
