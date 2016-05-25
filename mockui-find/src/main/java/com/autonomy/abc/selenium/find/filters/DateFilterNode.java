package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DateFilterNode extends FilterNode{

    DateFilterNode(WebElement element, WebDriver webDriver){
        super(element,webDriver);
    }

    public List<WebElement> getChildren(){
        return getContainer().findElements(By.xpath((".//tr[@data-filter-id]/td[2]")));
    }

    @Override
    public List<String> getChildNames(){
        return ElementUtil.getTexts(getChildren());
    }

}
