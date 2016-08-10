package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindParametricFilter extends Checkbox {
    private final By checkbox = By.className("parametric-value-count-cell");

    FindParametricFilter(final WebElement element, final WebDriver driver) {
        super(element, element.findElement(By.className("check-cell")), driver);
    }

    @Override
    public String getName() {
        return name().getText().trim();
    }

    public WebElement name(){return findElement(By.className("parametric-value-text")); }

    @Override
    public boolean isChecked() {
        return !ElementUtil.hasClass("hide", getOuterBoxElement().findElement(By.className("hp-check")));
    }

    public int getResultsCount() {
        return getResultCount(checkbox);
    }
}
