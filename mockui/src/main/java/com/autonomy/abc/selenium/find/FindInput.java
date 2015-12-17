package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FindInput extends AppElement {
    public FindInput(WebDriver driver) {
        super(driver.findElement(By.className("find-input")), driver);
    }

    public String getSearchTerm() {
        return $el().getAttribute("value");
    }
}
