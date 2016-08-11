package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.find.numericWidgets.NumericWidget;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class GraphFilterContainer extends FilterContainer {

    private final WebDriver driver;
    private final WebElement container;

    GraphFilterContainer(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
        container = element;
    }

    public NumericWidget getChart(){
        return new NumericWidget(driver,container);
    }

    public WebElement graph(){
        return getContainer().findElement(By.cssSelector("div.collapse:nth-child(2)"));
    }

    //REFACTOR
    //gets the graph header
    @Override
    public WebElement filterCategory(){
        return getContainer().findElement(By.cssSelector(".collapsible-header h4"));
    }
}
