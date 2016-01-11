package com.autonomy.abc.selenium.page.indexes;

import com.autonomy.abc.selenium.page.SAASPageBase;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexConfigStepTab;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexNameWizardStepTab;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexSummaryStepTab;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CreateNewIndexPage extends SAASPageBase {
    public CreateNewIndexPage(WebDriver driver) {
        super(driver);
    }

    public static CreateNewIndexPage make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("actions")));
        return new CreateNewIndexPage(driver);
    }

    /* navigation */

    public WebElement continueWizardButton() {
        return menuButton("Next");
    }

    public WebElement prevButton() {
        return menuButton("Previous");
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
}
