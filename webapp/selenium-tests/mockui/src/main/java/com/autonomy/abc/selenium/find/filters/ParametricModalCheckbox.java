package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.CheckboxBase;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


public class ParametricModalCheckbox extends CheckboxBase {

    WebElement element;

    ParametricModalCheckbox(final WebElement element) {
        this.element = element;
    }

    public String getName(){
        return element.findElement(By.className("field-value")).getText();
    }

    public int getResultsCount() {
        final String[] splitTitle = element.getText().split("\\(");
        final String spanResultCount = splitTitle[splitTitle.length - 1];

        return Integer.parseInt(spanResultCount.substring(0, spanResultCount.length() - 1));
}

    @Override
    public boolean isChecked(){return ElementUtil.hasClass("checked", element.findElement(By.cssSelector(".icheckbox-hp")));}

    @Override
    public void toggle() {
        element.click();
    }
}
