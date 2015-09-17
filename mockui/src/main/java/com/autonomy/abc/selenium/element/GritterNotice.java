package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class GritterNotice extends AppElement {
    public GritterNotice(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public String getTitle() {
        return findElement(By.className("gritter-title")).getText();
    }

    public String getBody() {
        return findElement(By.tagName("p")).getText();
    }

    public void close() {
        findElement(By.className("gritter-close")).click();
    }

    public static ExpectedCondition<?> notificationAppears() {
        // TODO: does this need "refreshed"?
        return ExpectedConditions.refreshed(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("gritter-item")));
    }

    public static ExpectedCondition<?> notificationsDisappear() {
        return ExpectedConditions.invisibilityOfElementLocated(By.id("gritter-notice-wrapper"));
    }
}
