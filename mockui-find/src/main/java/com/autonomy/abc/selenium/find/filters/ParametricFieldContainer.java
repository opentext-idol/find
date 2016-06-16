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

    ParametricFieldContainer(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
    }

    @Override
    //check where this is used....
    public String getParentName(){
        return getParent().getText().split(" \\(")[0];
    }

    public List<WebElement> getChildren(){
        return getContainer().findElements(By.className("parametric-value-name"));
    }

    @Override
    public List<String> getChildNames() {
        return ElementUtil.getTexts(getChildren());
    }

    //visible only
    private List<WebElement> getFullChildrenElements(){
        return getContainer().findElements(By.cssSelector(".parametric-value-element:not(.hide)"));
    }

    @Override
    public Iterator<FindParametricCheckbox> iterator() {
        return values().iterator();
    }

    public List<FindParametricCheckbox> values() {
        final List<FindParametricCheckbox> boxes = new ArrayList<>();
        for (final WebElement el : getFullChildrenElements()) {
            boxes.add(new FindParametricCheckbox(el, driver));
        }
        return boxes;
    }

    public void seeAll(){
        //HSOD?!
        getContainer().findElement(By.cssSelector(".show-all")).click();
    }

    public WebElement numberSelectedSubtitle(){return getContainer().findElement(By.cssSelector("collapsible-subtitle"));}
}
