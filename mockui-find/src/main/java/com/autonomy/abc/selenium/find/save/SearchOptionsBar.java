package com.autonomy.abc.selenium.find.save;

import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class SearchOptionsBar {
    private final WebDriver driver;
    private final WebElement bar;

    public SearchOptionsBar(final WebDriver driver) {
        this.driver = driver;
        this.bar = driver.findElement(By.cssSelector(".query-service-view-container > :not(.hide):not(.search-tabs-container) .search-options-container"));
    }

    public WebElement saveAsButton() {
        return findElement(By.className("show-save-as-button"));
    }

    public FormInput searchTitleInput() {
        return new FormInput(findElement(By.className("search-title-input")), driver);
    }

    void confirmSave() {
        final WebElement confirmButton = saveConfirmButton();
        confirmButton.click();
        new WebDriverWait(driver, 20)
                .withMessage("saving a search")
                .until(stalenessOf(confirmButton));
    }

    public WebElement saveConfirmButton() {
        return findElement(By.className("save-title-confirm-button"));
    }

    public String getSaveErrorMessage() {
        return findElement(By.className("search-title-error-message")).getText();
    }

    public WebElement searchTypeButton(final SearchType type) {
        return ElementUtil.ancestor(findElement(By.cssSelector("input[type='radio'][value='" + type + "']")), 2);
    }

    void openDeleteModal() {
        extraOptions().select("Delete");
    }

    public void openSnapshotAsQuery() {
        extraOptions().select("Open as Query");
    }

    private Dropdown extraOptions() {
        final WebElement dropdown = findElement(By.cssSelector("[data-toggle=dropdown]"));
        return new Dropdown(ElementUtil.getParent(dropdown), driver);
    }

    void confirmDelete() {
        final WebElement deleteModal = new WebDriverWait(driver, 10)
                .until(visibilityOfElementLocated(By.className("modal-content")));
        deleteModal.findElement(By.className("okButton")).click();
        new WebDriverWait(driver, 10).until(stalenessOf(deleteModal));
    }

    private WebElement findElement(final By locator) {
        return bar.findElement(locator);
    }
}
