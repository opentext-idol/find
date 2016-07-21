package com.autonomy.abc.selenium.find.filters;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class GraphFilterContainer extends FilterContainer {

    private final WebDriver driver;

    GraphFilterContainer(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
    }

    public String filteringInfo(){
        return getContainer().findElement(By.className("collapsible-subtitle")).getText();
    }

    //gets the graph header
    @Override
    public WebElement getParent(){
        return getContainer().findElement(By.cssSelector("collapsible-header"));
    }
    public WebElement graph(){
        return getContainer().findElement(By.cssSelector("div.collapse:nth-child(2)"));
    }

}
