package com.autonomy.abc.selenium.page.indexes.wizard;

import com.autonomy.abc.selenium.page.SAASPageBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IndexSummaryStepTab extends SAASPageBase {
    public IndexSummaryStepTab(WebDriver driver) {
        super(driver);
    }

    public static IndexSummaryStepTab make(WebDriver driver){
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id("indexWizardSummaryStepDescription")));
        return new IndexSummaryStepTab(driver);
    }

    public WebElement indexDescriptionLabel() {
        return findElement(By.id("indexWizardSummaryStepDescription")).findElement(By.tagName("label"));
    }

    public WebElement indexConfigurationsLabel() {
        return findElement(By.id("indexWizardSummaryStepConfigurations")).findElement(By.tagName("label"));
    }
}
