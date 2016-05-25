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

    public List<WebElement> getChildren(){
        return container.findElements(By.className("parametric-value-name"));
    }

    @Override
    public List<String> getChildNames() {
        return ElementUtil.getTexts(getChildren());
    }

}