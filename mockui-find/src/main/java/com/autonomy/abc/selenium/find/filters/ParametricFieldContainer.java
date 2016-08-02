package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParametricFieldContainer extends ListFilterContainer implements Iterable<FindParametricCheckbox> {
    private final WebDriver driver;

    ParametricFieldContainer(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
    }

    @Override
    public String getParentName(){
        if(isCollapsed()){
            expand();
        }
        return getParent().getText().split(" \\(")[0];
    }

    public List<WebElement> getChildren(){
        return getContainer().findElements(By.className("parametric-value-name"));
    }

    @Override
    public List<String> getChildNames() {
        final boolean startedCollapsed = this.isCollapsed();

        // text cannot be read if it is not visible
        if (startedCollapsed) {
            this.expand();
        }

        final List<String> childNames = ElementUtil.getTexts(getChildren());

        // restore collapsed state
        if (startedCollapsed) {
            this.collapse();
        }

        return childNames;
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
