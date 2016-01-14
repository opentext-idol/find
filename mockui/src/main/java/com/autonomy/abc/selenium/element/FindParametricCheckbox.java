package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FindParametricCheckbox extends Checkbox {
    WebElement box;

    public FindParametricCheckbox(WebElement element, WebDriver driver) {
        super(element, driver);
        box = findElement(By.className("fa-check"));
    }

    @Override
    public String getName() {
        return findElement(By.cssSelector("td:not(.check-cell)")).getText().split("\\(")[0].trim();
    }

    @Override
    public boolean isChecked() {
        return !ElementUtil.hasClass("hide", box);
    }
}
