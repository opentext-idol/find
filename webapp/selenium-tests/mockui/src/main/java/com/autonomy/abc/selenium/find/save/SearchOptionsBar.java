package com.autonomy.abc.selenium.find.save;

import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.Menu;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class SearchOptionsBar {
    private final WebDriver webDriver;
    private final WebElement bar;

    public SearchOptionsBar(final WebDriver driver) {
        webDriver = driver;
        bar = driver.findElement(By.cssSelector(".query-service-view-container > :not(.hide):not(.search-tabs-container) .search-options-container"));
    }

    public WebElement saveAsButton(final SearchType type){
        return findElement(By.cssSelector(".show-save-as[data-search-type='"+type+"']"));
    }

    FormInput searchTitleInput() {
        return new FormInput(findElement(By.className("search-title-input")), webDriver);
    }

    public void cancelSave(){
        bar.findElement(By.className("save-title-cancel-button")).click();
    }

    void confirmSave() {
        final WebElement confirmButton = saveConfirmButton();
        confirmButton.click();
        new WebDriverWait(webDriver, 120)
                .withMessage("saving a search")
                .until((ExpectedCondition<Boolean>) driver -> bar.findElements(By.cssSelector(".search-title-input-container")).isEmpty());
    }

    public WebElement saveConfirmButton() {
        return findElement(By.className("save-title-confirm-button"));
    }

    public WebElement renameButton() {
        return findElement(By.cssSelector(".show-rename-button"));
    }

    public String getSaveErrorMessage() {
        return findElement(By.className("search-title-error-message")).getText();
    }

    public void delete() {
        openDeleteModal();
        confirmModalOperation();
        Waits.loadOrFadeWait();
    }

    private void openDeleteModal() {
        extraOptions().select("Delete");
    }

    public void openSnapshotAsQuery() {
        extraOptions().select("Open as Query");
    }

    public void openResetModal() {
        extraOptions().select("Reset");
    }

    //Ellipsis unicode character used
    public void exportResultsToCSV() { extraOptions().select("Export Results to CSV\u2026");}

    private Menu<String> extraOptions() {
        final WebElement dropdown = findElement(By.cssSelector("[data-toggle=dropdown]"));
        return new Dropdown(ElementUtil.getParent(dropdown), webDriver);
    }

    public void confirmModalOperation() {
        final WebElement confirmModal = new WebDriverWait(webDriver, 10)
                .until(visibilityOfElementLocated(By.className("modal-content")));

        confirmModal.findElement(By.className("okButton")).click();
        new WebDriverWait(webDriver, 10).until(stalenessOf(confirmModal));
    }

    private WebElement findElement(final By locator) {
        return bar.findElement(locator);
    }
}
