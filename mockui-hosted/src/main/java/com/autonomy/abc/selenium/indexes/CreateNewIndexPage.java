package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CreateNewIndexPage extends ICMAPageBase {
    private CreateNewIndexPage(WebDriver driver) {
        super(driver);
    }

    /* navigation */
    public WebElement continueWizardButton() {
        return menuButton("Next");
    }

    public WebElement finishWizardButton() {
        return menuButton("Finish");
    }

    public WebElement cancelWizardButton() {
        return menuButton("Cancel");
    }

    private WebElement menuButton(String text) {
        return findElement(By.className("actions")).findElement(By.xpath(".//a[contains(text(), '" + text + "')]"));
    }

    public IndexNameWizardStepTab getIndexNameWizardStepTab() {
        return IndexNameWizardStepTab.make(getDriver());
    }

    public IndexConfigStepTab getIndexConfigStepTab() {
        return IndexConfigStepTab.make(getDriver());
    }

    public IndexSummaryStepTab getIndexSummaryStepTab() {
        return IndexSummaryStepTab.make(getDriver());
    }

    public static class Factory extends ICMAPageFactory<CreateNewIndexPage> {
        public Factory() {
            super(CreateNewIndexPage.class);
        }

        @Override
        public CreateNewIndexPage create(WebDriver context) {
            new WebDriverWait(context, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("content")));
            return new CreateNewIndexPage(context);
        }
    }
}
