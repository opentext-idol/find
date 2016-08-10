package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParametricFieldContainer extends ListFilterContainer implements Iterable<FindParametricFilter> {
    private final WebDriver driver;

    ParametricFieldContainer(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
    }

    @Override
    public String getParentName(){
        if(isCollapsed()){
            expand();
        }
        return getParent().getText().split(" \\(")[0];
    }

    public String getFilterNumber() {
        return getParent().getText().split(" \\(")[1].replaceAll("[()]","");
    }

    public List<WebElement> getFilters(){
        return getContainer().findElements(By.className("parametric-value-name"));
    }

    @Override
    public List<String> getFilterNames() {
        final boolean startedCollapsed = this.isCollapsed();

        // text cannot be read if it is not visible
        if (startedCollapsed) {
            this.expand();
        }

        final List<String> childNames = ElementUtil.getTexts(getFilters());

        // restore collapsed state
        if (startedCollapsed) {
            this.collapse();
        }

        return childNames;
    }

    //visible only
    private List<WebElement> getFullChildrenElements(){
        return getContainer().findElements(By.cssSelector(".parametric-value-element:not(.hide)"));
    }

    @Override
    public Iterator<FindParametricFilter> iterator() {
        return values().iterator();
    }

    public List<FindParametricFilter> values() {
        final List<FindParametricFilter> boxes = new ArrayList<>();
        for (final WebElement el : getFullChildrenElements()) {
            boxes.add(new FindParametricFilter(el, driver));
        }
        return boxes;
    }

    public void seeAll(){
        getContainer().findElement(By.cssSelector(".show-all")).click();
    }
}
