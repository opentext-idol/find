package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.*;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OPPromotionsDetailPage extends PromotionsDetailPage {
    private AppElement fieldTextContainer;

    public OPPromotionsDetailPage(WebDriver driver) {
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

    public WebElement fieldTextRemoveButton() {
        return getParent(findElement(By.cssSelector(".promotion-field-text .fa-remove")));
    }

    public void addFieldText(final String fieldText) {
        fieldTextAddButton().click();
        loadOrFadeWait();
        fieldTextInput().setAndSubmit(fieldText);
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(fieldTextRemoveButton()));
    }

    public String getFieldTextError() {
        try {
            return fieldTextContainer.findElement(By.cssSelector(".field-text-error")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
