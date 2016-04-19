package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

class FindIndexCheckbox extends Checkbox {
    FindIndexCheckbox(WebElement element, WebDriver driver) {
        super(element, element.findElement(By.className("check-cell")), driver);
    }

    @Override
    public String getName() {
        return findElement(By.className("database-name")).getText();
    }

    @Override
    public boolean isChecked() {
        return ElementUtil.hasClass("hp-check", getOuterBoxElement().findElement(By.tagName("i")));
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
