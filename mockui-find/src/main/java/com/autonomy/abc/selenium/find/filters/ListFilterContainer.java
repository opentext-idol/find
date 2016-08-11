package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public abstract class ListFilterContainer extends FilterContainer {

    ListFilterContainer(final WebElement element, final WebDriver webDriver) {
        super(element,webDriver);
    }

    public abstract List<String> getFilterNames();
}

