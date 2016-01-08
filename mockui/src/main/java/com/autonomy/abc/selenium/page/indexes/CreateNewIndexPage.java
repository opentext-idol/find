package com.autonomy.abc.selenium.page.indexes;

import com.autonomy.abc.selenium.element.ChevronContainer;
import com.autonomy.abc.selenium.element.Collapsible;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.indexes.IndexConfigStepTab;
import com.autonomy.abc.selenium.indexes.IndexNameWizardStep;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexNameWizardStepTab;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class CreateNewIndexPage extends SAASPageBase {
    public CreateNewIndexPage(WebDriver driver) {
        super(driver);
    }

    public static CreateNewIndexPage make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("actions")));
        return new CreateNewIndexPage(driver);
    }

    /* index name step */
    public FormInput indexNameInput() {
        return new FormInput(findElement(By.cssSelector("[name='indexName']")), getDriver());
    }

    public FormInput displayNameInput() {
        return new FormInput(findElement(By.cssSelector("[name='indexDisplayName']")), getDriver());
    }

    /* index configuration step */
    public void setIndexFields(List<String> indexFields) {
        advancedOptions().expand();
        String joined = StringUtils.join(indexFields, ",");
        indexFieldsInput().setValue(joined);
    }

    public void setParametricFields(List<String> parametricFields) {
        advancedOptions().expand();
        String joined = StringUtils.join(parametricFields, ",");
        parametricFieldsInput().setValue(joined);
    }

    public FormInput indexFieldsInput() {
        return fieldInput("indexFields");
    }

    public FormInput parametricFieldsInput() {
        return fieldInput("parametricFields");
    }

    private FormInput fieldInput(String inputType) {
        WebElement input = findElement(By.cssSelector("[for='" + inputType + "'] + div input"));
        return new FormInput(input, getDriver());
    }

    public WebElement advancedIndexFieldsInformation() {
        return findElement(By.cssSelector("[for='indexFields'] + div i"));
    }

    public WebElement advancedParametricFieldsInformation() {
        return findElement(By.cssSelector("[for='parametricFields'] + div i"));
    }

    public Collapsible advancedOptions() {
        WebElement panel = findElement(By.className("panel"));
        return new ChevronContainer(ElementUtil.ancestor(panel, 1));
    }

    /* summary step */
    public WebElement summaryStepIndexDescriptionLabel() {
        return findElement(By.id("indexWizardSummaryStepDescription")).findElement(By.tagName("label"));
    }

    public WebElement summaryStepIndexConfigurationsLabel() {
        return findElement(By.id("indexWizardSummaryStepConfigurations")).findElement(By.tagName("label"));
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
}
