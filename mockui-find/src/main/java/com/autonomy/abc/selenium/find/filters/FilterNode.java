package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.ChevronContainer;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class FilterNode implements Collapsible{

    private final WebElement container;
    private final WebDriver driver;
    private final Collapsible collapsible;

    public FilterNode(WebElement element, WebDriver webDriver) {
        container=element;
        driver=webDriver;
        collapsible=new ChevronContainer(container,driver);
    }

    public String getParentName(){
       return getParent().getText();
    }

    public WebElement getParent(){
        WebElement filterElement = container.findElement(By.xpath(".//ancestor::div[contains(@class,'collapse')]"));
        return ElementUtil.getFirstChild(filterElement.findElement(By.xpath(".//preceding-sibling::div")));
    }

    public WebElement findFilterType(){
        return container.findElement(By.tagName("h4"));
    }

    public List<String> getChildNames(){
        List<WebElement> children = container.findElements(By.cssSelector(".parametric-value-name"));
        children.addAll(container.findElements(By.cssSelector("[data-filter-id] > td:nth-child(2)")));
        children.addAll(container.findElements(By.className("database-name")));
        return ElementUtil.getTexts(children);
    }

    protected WebElement getContainer(){
        return container;
    }

    public String toString(){
        return findFilterType().getText();
    }

    @Override
    public void expand() {
        collapsible.expand();
    }

    @Override
    public void collapse(){
        collapsible.collapse();
    };

    @Override
    public boolean isCollapsed() {
        return collapsible.isCollapsed();
    }


}

