package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Wizard {
    private WebElement page;
    private WebDriver driver;

    public Wizard(AppElement page) {
        this.page = page;
        this.driver = page.getDriver();
    }

    /**
     * Just make sure that the next wizard step loads properly
     * as check for visibility does not work as expected
     */
    public void loadOrFadeWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private WebElement findElement(By by) {
        return page.findElement(by);
    }

    private WebElement getVisibleElement(By by) {
        return new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.className("current-step"))).findElement(by);
    }

    public WebElement option(String name) {
        return findElement(By.cssSelector("[data-option='" + name + "']"));
    }

    public WebElement continueButton() {
        return getVisibleElement(By.className("next-step"));
    }

    public WebElement cancelButton() {
        return getVisibleElement(By.className("cancel-wizard"));
    }

    public WebElement finishButton() {
        return getVisibleElement(By.className("finish-step"));
    }

    public WebElement button(String className) {
        return getVisibleElement(By.cssSelector("button." + className));
    }

    public WebElement backButton() {
        return getVisibleElement(By.xpath(".//a[text()[contains(., 'Back')]]"));
    }

    // only works on wizard steps with single input element
    public FormInput formInput() {
        return new FormInput(getVisibleElement(By.tagName("input")), driver);
    }

    public FormInput textarea() {
        return new FormInput(getVisibleElement(By.tagName("textarea")), driver);
    }

    public String getTitle() {
        return findElement(By.cssSelector(".current-step-pill .current-step-title")).getText();
    }
}
