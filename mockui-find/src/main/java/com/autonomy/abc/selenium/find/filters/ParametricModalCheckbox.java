package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ParametricModalCheckbox extends Checkbox {
    private final By count = By.tagName("label");

    ParametricModalCheckbox(final WebElement element, final WebDriver driver) {
        super(element, element.findElement(By.cssSelector(".icheckbox-hp")), driver);
    }

    @Override
    public String getName(){return findElement(By.cssSelector(".field-value")).getText();}

    public int getResultsCount() {
        return getResultCount(count);
}

    @Override
    public boolean isChecked(){return ElementUtil.hasClass("checked", getOuterBoxElement());}
}
