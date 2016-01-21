package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LabelBox implements Removable {
    private AppElement element;

    public LabelBox(AppElement element) {
        this.element = element;
    }

    public LabelBox(WebElement element, WebDriver driver) {
        this(new AppElement(element, driver));
    }

    @Override
    public boolean isRemovable() {
        return element.findElements(By.cssSelector(".fa-close, .fa-times, .fa-remove")).size() > 0;
    }

    @Override
    public boolean isRefreshing() {
        return element.findElements(By.className("fa-refresh")).size() > 0;
    }

    @Override
    public void removeAsync() {
        element.findElement(By.cssSelector(".fa-close, .fa-times, .fa-remove")).click();
    }

    @Override
    public void removeAndWait() {
        removeAndWait(20);
    }

    @Override
    public void removeAndWait(int timeout) {
        removeAsync();
        // the element itself can go stale instantly - must instead use the parent
        new WebDriverWait(element.getDriver(), timeout).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("..//*[contains(@class, 'fa-refresh')]")));
    }

    @Override
    public void click() {
        element.click();
    }

    @Override
    public String getText(){
        return element.getText();
    }
}
