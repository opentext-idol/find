package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.ChevronContainer;
import com.hp.autonomy.frontend.selenium.element.Collapsible;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class FilterContainer implements Collapsible{

    private final WebElement container;
    private final Collapsible collapsible;

    FilterContainer(final WebElement element, final WebDriver webDriver) {
        container=element;
        collapsible=new ChevronContainer(container, webDriver);
    }

    //REFACTOR START
    public String getParentName(){
        return getParent().getText();
    }

    protected WebElement getParent(){
        final WebElement filterElement = container.findElement(By.xpath(".//ancestor::div[contains(@class,'collapse')]"));
        return ElementUtil.getFirstChild(filterElement.findElement(By.xpath(".//preceding-sibling::div")));
    }

    private WebElement findFilterType(){
        return container.findElement(By.tagName("h4"));
    }

    WebElement getContainer(){
        return container;
    }

    public String toString(){
        return findFilterType().getText();
    }
    //REFACTOR END

    @Override
    public void expand() {
        collapsible.expand();
    }

    @Override
    public void collapse(){
        collapsible.collapse();
    }

    @Override
    public boolean isCollapsed() {
        return collapsible.isCollapsed();
    }

}
