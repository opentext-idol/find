package com.autonomy.abc.selenium.find.save;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;

public class SearchOptionsBar {
    private final WebDriver driver;
    private final WebElement bar;

    public SearchOptionsBar(WebDriver driver) {
        this.driver = driver;
        this.bar = driver.findElement(By.className("search-options-container"));
    }

    WebElement saveAsButton() {
        return findElement(By.className("show-save-as-button"));
    }

    FormInput searchTitleInput() {
        return new FormInput(findElement(By.className("search-title-input")), driver);
    }

    void confirmSave() {
        WebElement confirmButton = saveConfirmButton();
        confirmButton.click();
        new WebDriverWait(driver, 20)
                .withMessage("saving a search")
                .until(stalenessOf(confirmButton));
    }

    private WebElement saveConfirmButton() {
        return findElement(By.className("save-title-confirm-button"));
    }

    WebElement searchTypeButton(SearchType type) {
        return ElementUtil.ancestor(findElement(By.cssSelector("input[type='radio'][value='" + type + "']")), 2);
    }

    private WebElement findElement(By locator) {
        return bar.findElement(locator);
    }
}
