package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InlineEdit implements Editable {
    private AppElement element;

    public InlineEdit(AppElement element) {
        this.element = element;
    }

    public InlineEdit(WebElement element, WebDriver driver) {
        this(new AppElement(element, driver));
    }

    @Override
    public String getValue() {
        return element.findElement(By.className("inline-edit-current-value")).getText();
    }

    @Override
    public void setValueAsync(String value) {
        element.findElement(By.className("inline-edit-open-form")).click();
        waitForUpdate();
        new FormInput(element.findElement(By.className("inline-edit-form")), element.getDriver()).setAndSubmit(value);
    }

    @Override
    public void setValueAndWait(String value) {
        setValueAsync(value);
        waitForUpdate();
    }

    @Override
    public void waitForUpdate() {
        new WebDriverWait(element.getDriver(), 30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("fa-refresh")));
    }

    @Override
    public AppElement getElement() {
        return element;
    }

}
