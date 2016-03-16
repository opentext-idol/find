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

    // TODO: will not work if there are already notifications
    public static ExpectedCondition<WebElement> notificationAppears() {
        return ExpectedConditions.visibilityOfElementLocated(By.className("gritter-item"));
    }

    public static ExpectedCondition<WebElement> notificationContaining(String text) {
        return ExpectedConditions.visibilityOfElementLocated(By.xpath(".//*[contains(@class, 'gritter-item')]//*[contains(text(), '" + text + "')]/../.."));
    }

    public static ExpectedCondition<?> notificationsDisappear() {
        return ExpectedConditions.invisibilityOfElementLocated(By.id("gritter-notice-wrapper"));
    }
}
