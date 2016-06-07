package com.autonomy.abc.selenium.find.bi;


import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ChosenDrop {
    private AppElement element;
    private WebElement button;
    private WebElement menu;

    private ChosenDrop(AppElement element){
        this.element = element;
        this.button = element.findElement(By.className("chosen-container-single"));
        this.menu = element.findElement(By.className("chosen-results"));
    }

    ChosenDrop(WebElement element, WebDriver driver) {
        this(new AppElement(element, driver));
    }

    public void toggle() {
        this.button.click();
    }

    public boolean isOpen() {
        return this.menu.isDisplayed();
    }

    public void open() {
        if(!this.isOpen()) {
            this.toggle();
            (new WebDriverWait(this.element.getDriver(), 5L)).until(ExpectedConditions.visibilityOf(this.menu));
        }

    }

    public void close() {
        if(this.isOpen()) {
            this.toggle();
        }

    }
    public List<WebElement> getItems() {
        return this.menu.findElements(By.tagName("li"));
    }

    public WebElement getItem(String text) {
        return this.menu.findElement(By.xpath(".//*[text()[contains(., \'" + text + "\')]]"));
    }

    public void select(String text) {
        this.open();
        this.getItem(text).click();
    }

    public void selectIthItem(int i){
        open();
        getItems().get(i).click();}

    public String getValue() {
        return this.button.getText();
    }


}
