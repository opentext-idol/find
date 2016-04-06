package com.autonomy.abc.selenium.search;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EditDocumentReferencesPage extends SearchBase {

    private EditDocumentReferencesPage(final WebDriver driver) {
        super(driver.findElement(By.className("wrapper-content")), driver);
    }

    private static void waitForLoad(WebDriver driver) {
        new WebDriverWait(driver, 30)
                .withMessage("loading edit document references page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("promotions-bucket-well")));
    }

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    public WebElement saveButton() {
        return findElement(By.xpath(".//button[text() = 'Save']"));
    }

    public WebElement cancelButton() {
        return findElement(By.xpath(".//*[contains(text(), 'Cancel')]"));
    }

    public static class Factory extends SOPageFactory<EditDocumentReferencesPage> {
        public Factory() {
            super(EditDocumentReferencesPage.class);
        }

        @Override
        public EditDocumentReferencesPage create(WebDriver context) {
            waitForLoad(context);
            return new EditDocumentReferencesPage(context);
        }
    }
}
