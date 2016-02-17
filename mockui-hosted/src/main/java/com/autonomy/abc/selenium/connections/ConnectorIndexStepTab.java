package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ConnectorIndexStepTab extends ICMAPageBase {

    private ConnectorIndexStepTab(WebDriver driver){
        super(driver);
    }

    static ConnectorIndexStepTab make(WebDriver driver) {
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

    private WebElement getIndexSearchBox(){
        return getDriver().findElement(By.className("chosen-single"));
    }

    private List<WebElement> getExistingIndexes() {
        return getDriver().findElements(By.cssSelector(".chosen-results li"));
    }

    private WebElement modalOKButton() {
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
        String displayName = getIndexSearchBox().findElement(By.tagName("span")).getText();
        return new Index(null, displayName);
    }

    private boolean isDropdownOpen() {
        return getIndexSearchBox().findElement(By.xpath(".//..")).getAttribute("class").contains("chosen-with-drop");

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
        String displayName = findElement(By.cssSelector(".selectedIndexNameContainer .ng-binding")).getText();
        return new Index(null, displayName);
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

    private static class IndexNotFoundException extends RuntimeException {
        public IndexNotFoundException(Index index){
            super(index + " not found");
        }
    }
}
