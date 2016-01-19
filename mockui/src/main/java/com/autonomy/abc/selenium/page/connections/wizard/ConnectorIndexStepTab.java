package com.autonomy.abc.selenium.page.connections.wizard;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ConnectorIndexStepTab extends SAASPageBase {

    private ConnectorIndexStepTab(WebDriver driver){
        super(driver);
    }

    public static ConnectorIndexStepTab make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.name("indexStepForm")));
        return new ConnectorIndexStepTab(driver);
    }

    public FormInput indexNameInput(){
        return new FormInput(findElement(By.cssSelector("[name='indexName']")), getDriver());
    }

    public FormInput indexDisplayNameInput(){
        return new FormInput(findElement(By.cssSelector("[name='displayName']")), getDriver());
    }

    public void setIndexName(String name){
        indexNameInput().setValue(name);
    }

    public void setIndexDisplayName(String displayName){
        indexDisplayNameInput().setValue(displayName);
    }

    public WebElement selectIndexButton(){
        return findElement(By.xpath("//button[text()='Select index']"));
    }

    public WebElement getIndexSearchBox(){
        return getDriver().findElement(By.className("chosen-single"));
    }

    public List<WebElement> getExistingIndexes() {
        return getDriver().findElements(By.cssSelector(".chosen-results li"));
    }

    public WebElement modalOKButton() {
        return getDriver().findElement(By.cssSelector(".modal-footer [type='submit']"));
    }

    public void selectFirstIndex() {
        selectNthIndex(1);
    }

    public void selectLastIndex() {
        selectNthIndex(getDriver().findElements(By.cssSelector(".chosen-results li")).size());
    }

    public void selectNthIndex(int n){
        getIndexSearchBox().click();
        getDriver().findElement(By.cssSelector(".chosen-results li:nth-child(" + n + ")")).click();
    }

    public Index getChosenIndexInModal() {
        return new Index(getIndexSearchBox().findElement(By.tagName("span")).getText());
    }

    private boolean isDropdownOpen() {
        if(getIndexSearchBox().findElement(By.xpath(".//..")).getAttribute("class").contains("chosen-with-drop")){
            return true;
        }

        return false;
    }

    public void closeDropdown(){
        if(isDropdownOpen()){
            getIndexSearchBox().click();
        }
    }

    private boolean isModalOpen(){
        try {
            if (getDriver().findElement(By.className("modal")).isDisplayed()) {
                return true;
            }
        } catch (Exception e) { /* Modal not open */ }

        return false;
    }

    public void closeModal(){
        if(isModalOpen()){
            getDriver().findElement(By.xpath("//div[contains(@class,'modal-footer')]/button[text()='Cancel']")).click();
        }
    }

    //TODO change so that it gets display and normal name (if possible)
    public Index getChosenIndexOnPage() {
        return new Index(findElement(By.cssSelector(".selectedIndexNameContainer .ng-binding")).getText());
    }

    public void selectIndex(Index index) {
        getIndexSearchBox().click();

        for(WebElement existingIndex : getExistingIndexes()){
            if(existingIndex.getText().equals(index.getDisplayName())){
                existingIndex.click();
                modalOKButton().click();
                //Need to wait for modal to disappear
                Waits.loadOrFadeWait();
                return;
            }
        }

        throw new IndexNotFoundException(index);
    }

    private class IndexNotFoundException extends RuntimeException {
        public IndexNotFoundException(String index){
            super("Index: '"+index+"' not found");
        }

        public IndexNotFoundException(Index index){
            this(index.getName());
        }
    }
}
