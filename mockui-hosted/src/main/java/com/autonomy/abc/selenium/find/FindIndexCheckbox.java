package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.Checkbox;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindIndexCheckbox extends Checkbox {
    public FindIndexCheckbox(WebElement element, WebDriver driver) {
        super(element, driver);
        box = findElement(By.className("check-cell"));
    }

    @Override
    public String getName() {
        return findElement(By.className("database-name")).getText();
    }

    @Override
    public boolean isChecked() {
        return ElementUtil.hasClass("hp-check", box.findElement(By.tagName("i")));
    }

    @Override
    public void check() {
        if(!isChecked()) {
            toggle();

            //Top level checkboxes may have to be cleared and THEN selected, so try again
            if (!isChecked()) {
                toggle();
            }
        }
    }
}
