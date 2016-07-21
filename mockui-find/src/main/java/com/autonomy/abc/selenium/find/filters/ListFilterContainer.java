package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ListFilterContainer extends FilterContainer {

    ListFilterContainer(final WebElement element, final WebDriver webDriver) {
        super(element,webDriver);
    }

    public List<String> getChildNames(){
        final List<WebElement> children = getContainer().findElements(By.cssSelector(".parametric-value-name"));
        children.addAll(getContainer().findElements(By.cssSelector("[data-filter-id] > td:nth-child(2)")));
        children.addAll(getContainer().findElements(By.className("database-name")));
        return ElementUtil.getTexts(children);
    }
}
