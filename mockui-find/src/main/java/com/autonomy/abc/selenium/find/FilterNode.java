package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.element.ChevronContainer;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FilterNode implements Collapsible{

    private final WebElement container;
    private final WebDriver driver;
    private final Collapsible collapsible;

    FilterNode(WebElement element, WebDriver webDriver) {
        container=element;
        driver=webDriver;
        collapsible=new ChevronContainer(container,driver);
    }

    @Override
    public void expand() {
        collapsible.expand();
    };

    @Override
    public void collapse(){
        collapsible.collapse();
    };

    @Override
    public boolean isCollapsed(){
        return collapsible.isCollapsed();
    };
}

