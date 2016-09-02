package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ParametricModalCheckbox extends Checkbox {

    ParametricModalCheckbox(final WebElement element, final WebDriver driver) {
        super(element, element.findElement(By.cssSelector(".icheckbox-hp")), driver);
    }

    @Override
    public String getName(){return findElement(By.cssSelector(".field-value")).getText();}

    public int getResultsCount() {
        String spanResultCount = $el().getText().split("\\(")[1];
        return Integer.parseInt(spanResultCount.substring(0, spanResultCount.length() - 1));
}

    @Override
    public boolean isChecked(){return ElementUtil.hasClass("checked", getOuterBoxElement());}
}
