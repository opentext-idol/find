package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindParametricCheckbox extends Checkbox {
    private final By checkbox = By.className("parametric-value-count-cell");

    FindParametricCheckbox(WebElement element, WebDriver driver) {
        super(element, element.findElement(By.className("check-cell")), driver);
    }

    @Override
    public String getName() {
        return findElement(By.className("parametric-value-text")).getText().trim();
    }

    @Override
    public boolean isChecked() {
        return !ElementUtil.hasClass("hide", getOuterBoxElement().findElement(By.className("hp-check")));
    }

    public int getResultsCount() {
        return getResultCount(checkbox);
    }
}
