package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OriginalDoc extends AppElement {
    private final WebDriver webDriver;

    public OriginalDoc(final WebDriver webDriver) {
        super((new WebDriverWait(webDriver, 2)
                .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))),webDriver);
        this.webDriver = webDriver;
    }

    public boolean hasContent() {
        return !findElements(By.xpath("//body/descendant::*")).isEmpty();
    }


}
