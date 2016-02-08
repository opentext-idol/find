package com.autonomy.abc.selenium.page.indexes;

import com.autonomy.abc.selenium.page.SAASPageBase;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexConfigStepTab;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexNameWizardStepTab;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexSummaryStepTab;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CreateNewIndexPage extends SAASPageBase {
    private CreateNewIndexPage(WebDriver driver) {
        super(driver);
    }

    /* navigation */
    public WebElement chooseIndexNameHeader() {
        return chooseTab(1);
    }

    public WebElement indexConfigurationHeader() {
        return chooseTab(2);
    }

    public WebElement summaryHeader() {
        return chooseTab(3);
    }

    private WebElement chooseTab(int tab) {
        return findElement(By.cssSelector("[role='tablist'] li:nth-child(" + tab + ")"));
    }

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

    public static class Factory implements ParametrizedFactory<WebDriver, CreateNewIndexPage> {
        @Override
        public CreateNewIndexPage create(WebDriver context) {
            new WebDriverWait(context, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("content")));
            return new CreateNewIndexPage(context);
        }
    }
}
