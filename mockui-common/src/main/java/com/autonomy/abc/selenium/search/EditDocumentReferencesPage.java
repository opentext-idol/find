package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.LabelBox;
import com.autonomy.abc.selenium.element.Removable;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class EditDocumentReferencesPage extends SearchBase implements AppPage {

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

    public List<String> promotionsBucketList() {
        return bucketList(this);
    }

    public List<Removable> promotionsBucketItems() {
        List<Removable> items = new ArrayList<>();
        for (WebElement element : promotionsBucketWebElements()) {
            items.add(new LabelBox(element, getDriver()));
        }
        return items;
    }

    public WebElement saveButton() {
        return findElement(By.xpath(".//button[text() = 'Save']"));
    }

    public WebElement cancelButton() {
        return findElement(By.xpath(".//*[contains(text(), 'Cancel')]"));
    }

    public static class Factory implements ParametrizedFactory<WebDriver, EditDocumentReferencesPage> {
        @Override
        public EditDocumentReferencesPage create(WebDriver context) {
            waitForLoad(context);
            return new EditDocumentReferencesPage(context);
        }
    }
}
