package com.autonomy.abc.selenium.element;

import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SOCheckbox extends Checkbox {
    public SOCheckbox(WebElement element, WebDriver driver) {
        super(element, element.findElement(By.className("icheckbox_square-green")), driver);
    }

    public String getName() {
        return findElement(By.tagName("label")).getText();
    }

    public boolean isChecked() {
        return ElementUtil.hasClass("checked", getOuterBoxElement());
    }

    public int getResultsCount() {
        return getResultCount(By.tagName("span"));
    }
}
