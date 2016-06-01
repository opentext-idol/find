package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;

public class ParametricFilterNode extends FilterNode {

    ParametricFilterNode(WebElement element, WebDriver webDriver) {
        super(element, webDriver);
    }

    public List<WebElement> getChildren(){
        return getContainer().findElements(By.className("parametric-value-name"));
    }

    @Override
    public List<String> getChildNames() {
        return ElementUtil.getTexts(getChildren());
    }

    public List<WebElement> getChildDocCount(){
        return getContainer().findElements(By.className("parametric-value-count"));
    }

    public List<WebElement> getFullChildrenElements(){
        return getContainer().findElements(By.className("parametric-value-element"));
    }
    public int getTotalDocNumber(){
        int total=0;
        for(WebElement element:getChildDocCount()){
            //gets text, trims brackets and casts to int
            total+=Integer.parseInt(element.getText().replaceAll("[()]",""));
        }
        return total;
    }


}