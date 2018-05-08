package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.CheckboxBase;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

class FindIndexCheckbox extends CheckboxBase {

    private WebElement element;

    FindIndexCheckbox(final WebElement element) {
        this.element = element;
    }

    public String getName() {
        return element.findElement(By.className("database-name")).getText();
    }

    @Override
    public boolean isChecked() {
        return ElementUtil.hasClass("hp-check", element.findElement(By.tagName("i")));
    }

    @Override
    public void toggle() {
        element.click();
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
