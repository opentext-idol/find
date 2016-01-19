package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindParametricCheckbox extends Checkbox {
    private final By checkbox = By.cssSelector("td:not(.check-cell)");

    public FindParametricCheckbox(WebElement element, WebDriver driver) {
        super(element, driver);
        box = findElement(By.className("check-cell"));
    }

    @Override
    public String getName() {
        return findElement(checkbox).getText().split("\\(")[0].trim();
    }

    @Override
    public boolean isChecked() {
        return !ElementUtil.hasClass("hide", box.findElement(By.className("fa-check")));
    }

    public int getResultsCount() {
        return getResultCount(checkbox);
    }
}
