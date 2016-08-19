package com.autonomy.abc.selenium.find.filters;

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
    public String filterCategoryName(){
        expand();
        return filterCategory().getText().split(" \\(")[0];
    }

    public String getFilterNumber() {
        return filterCategory().getText().split(" \\(")[1].replaceAll("[()]","");
    }

    public List<FindParametricFilter> getFilters() {
        final List<FindParametricFilter> boxes = new ArrayList<>();
        final List<WebElement> filters = getContainer().findElements(By.cssSelector(".parametric-value-element:not(.hide)"));

        for (final WebElement el : filters) {
            boxes.add(new FindParametricFilter(el, driver));
        }
        return boxes;
    }

    @Override
    public List<String> getFilterNames() {
        final boolean startedCollapsed = this.isCollapsed();

        // text cannot be read if it is not visible
        if (startedCollapsed) {
            this.expand();
        }

        List<String> filterNames = new ArrayList<>();
        for(FindParametricFilter filter: getFilters()) {
            filterNames.add(filter.getName());
        }

        // restore collapsed state
        if (startedCollapsed) {
            this.collapse();
        }

        return filterNames;
    }

    @Override
    public Iterator<FindParametricFilter> iterator() {
        return getFilters().iterator();
    }

    public void seeAll(){
        getContainer().findElement(By.cssSelector(".show-all")).click();
    }
}
