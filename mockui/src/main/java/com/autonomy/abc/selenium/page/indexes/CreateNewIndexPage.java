package com.autonomy.abc.selenium.page.indexes;

import com.autonomy.abc.selenium.page.AngularWizard;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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

    public WebElement indexNameInputElement(){
        return findElement(By.cssSelector("[name='indexName']"));
    }

    public WebElement advancedOptionsTab(){
        return findElement(By.id("advancedIndexPanelHeading"));
    }

    public WebElement advancedIndexFields(){
        return findElement(By.cssSelector("[for='indexFields'] + div input"));
    }

    public WebElement advancedIndexFieldsInformation(){
        return findElement(By.cssSelector("[for='indexFields'] + div i"));
    }

    public WebElement advancedParametricFields(){
        return findElement(By.cssSelector("[for='parametricFields'] + div input"));
    }

    public WebElement advancedParametricFieldsInformation(){
        return findElement(By.cssSelector("[for='parametricFields'] + div i"));
    }

    private WebElement chooseTab(int tab){
        return findElement(By.cssSelector("[role='tablist'] li:nth-child(" + tab + ")"));
    }

    public WebElement chooseIndexNameHeader(){
        return chooseTab(1);
    }

    public WebElement indexConfigurationHeader(){
        return chooseTab(2);
    }

    public WebElement summaryHeader(){
        return chooseTab(3);
    }

    public void inputIndexName(String name){
        indexNameInputElement().sendKeys(name);
    }

    private boolean isAdvancedOptionsCollapsed(){
        String y = advancedOptionsTab().getAttribute("aria-expanded");
        return y == null || y.equalsIgnoreCase("false");
    }

    private void openAdvancedOptions(){
        if(isAdvancedOptionsCollapsed()){
            advancedOptionsTab().click();
        }
    }

    public void toggleAdvancedOptions(){
        advancedOptionsTab().click();
    }

    public void inputIndexFields(List<String> indexFields){
        openAdvancedOptions();

        WebElement indexFieldsInput = advancedIndexFields();

        for(String indexField : indexFields){
            indexFieldsInput.sendKeys(indexField + ",");
        }

        indexFieldsInput.sendKeys(Keys.BACK_SPACE);
    }

    public void inputParametricFields(List<String> parametricFields){
        openAdvancedOptions();

        WebElement parametricFieldsInput = advancedParametricFields();

        for(String parametricField : parametricFields) {
            parametricFieldsInput.sendKeys(parametricField + ",");
        }

        parametricFieldsInput.sendKeys(Keys.BACK_SPACE);
    }

    private WebElement menuButton(String text) {
        return findElement(By.className("actions")).findElement(By.xpath(".//a[contains(text(), '" + text + "')]"));
    }

    public WebElement nextButton() {
        return menuButton("Next");
    }

    public WebElement prevButton(){
        return menuButton("Previous");
    }

    public WebElement finishButton() {
        return menuButton("Finish");
    }

    public WebElement cancelButton() {
        return menuButton("Cancel");
    }

    public void loadOrFadeWait() {
        getPage().loadOrFadeWait();
    }

}
