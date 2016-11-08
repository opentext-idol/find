package com.autonomy.abc.selenium.find.filters;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ParametricFieldContainer extends ListFilterContainer implements Iterable<FindParametricFilter> {

    ParametricFieldContainer(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
    }

    @Override
    public String filterCategoryName(){
        expand();
        return filterCategory().getText().split(" \\(")[0];
    }

    public int getFilterNumber() {
        return Integer.parseInt(filterCategory().getText().split(" \\(")[1].replaceAll("[()]",""));
    }

    public List<FindParametricFilter> getFilters() {
        final List<WebElement> filters = getContainer().findElements(By.cssSelector(".parametric-value-element:not(.hide)"));
        return filters.stream().map(FindParametricFilter::new).collect(Collectors.toList());
    }

    @Override
    public List<String> getFilterNames() {
        final boolean startedCollapsed = this.isCollapsed();

        // text cannot be read if it is not visible
        if (startedCollapsed) {
            this.expand();
        }

        List<String> filterNames = getFilters().stream().map(FindParametricFilter::getName).collect(Collectors.toList());

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
