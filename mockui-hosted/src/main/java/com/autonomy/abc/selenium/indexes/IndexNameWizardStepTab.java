package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IndexNameWizardStepTab extends ICMAPageBase {
    public IndexNameWizardStepTab(WebDriver driver) {
        super(driver);
    }

    static IndexNameWizardStepTab make(WebDriver driver) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.name("generalDetailsForm")));
        return new IndexNameWizardStepTab(driver);
    }

    public FormInput indexNameInput() {
        return new FormInput(findElement(By.cssSelector("[name='indexName']")), getDriver());
    }

    public FormInput displayNameInput() {
        return new FormInput(findElement(By.cssSelector("[name='indexDisplayName']")), getDriver());
    }
}
