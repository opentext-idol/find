package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

class IndexesTreeContainer extends ListFilterContainer {

    IndexesTreeContainer(final WebElement element, final WebDriver webDriver){
        super(element,webDriver);
    }

    public List<WebElement> getFilters(){
        return getContainer().findElements(By.className("database-name"));
    }

    @Override
    public List<String> getFilterNames(){
        return ElementUtil.getTexts(getFilters());
    }
}
