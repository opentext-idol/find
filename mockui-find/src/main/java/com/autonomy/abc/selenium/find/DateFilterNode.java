package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DateFilterNode extends FilterNode{

    private WebElement container;
    private WebDriver driver;

    DateFilterNode(WebElement element, WebDriver webDriver){
        super(element,webDriver);
        container = element;
    }

    protected DateFilterNode findNode(String filter){
        if(container.findElements(By.xpath("//h4[contains(text(),'"+filter+"')]")).size()>0) {
            WebElement element = ElementUtil.ancestor(findFilterType(), 2);
            return new DateFilterNode(element, driver);
        }
        else{
            return null;
        }
    }

    @Override
    public List<String> getChildNames(){
        return ElementUtil.getTexts(container.findElements(By.xpath((".//tr[@data-filter-id]/td[2]"))));
    }

}
