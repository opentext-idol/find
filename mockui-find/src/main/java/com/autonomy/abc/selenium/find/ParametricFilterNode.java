package com.autonomy.abc.selenium.find;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ParametricFilterNode extends FilterNode {

    ParametricFilterNode(WebElement element, WebDriver webDriver) {
        super(element, webDriver);
    }

    public ParametricFilterNode findNode(String name) {
        if(container.findElements(By.xpath("//*[contains(@data-field-display-name,'" + WordUtils.capitalize(name.toLowerCase()) + "')]")).size()>0){
            WebElement element = ElementUtil.getFirstChild(container.findElement
                        (By.xpath("//*[contains(@data-field-display-name,'" + WordUtils.capitalize(name.toLowerCase()) + "')]")));
            return new ParametricFilterNode(element,driver);}
        else{
                return null;
            }
    }

    @Override
    public List<String> getChildNames() {
        return ElementUtil.getTexts(container.findElements(By.xpath(".//*[contains(@class,'parametric-value-name')]")));
    }

}