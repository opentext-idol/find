package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParametricFieldContainer extends FilterContainer implements Iterable<FindParametricCheckbox> {
    private final WebDriver driver;

    ParametricFieldContainer(WebElement element, WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
    }

    public List<WebElement> getChildren(){
        return getContainer().findElements(By.className("parametric-value-name"));
    }

    @Override
    public List<String> getChildNames() {
        return ElementUtil.getTexts(getChildren());
    }

    private List<WebElement> getChildDocCount(){
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

    @Override
    public Iterator<FindParametricCheckbox> iterator() {
        return values().iterator();
    }

    public List<FindParametricCheckbox> values() {
        List<FindParametricCheckbox> boxes = new ArrayList<>();
        for (WebElement el : getFullChildrenElements()) {
            boxes.add(new FindParametricCheckbox(el, driver));
        }
        return boxes;
    }

}
