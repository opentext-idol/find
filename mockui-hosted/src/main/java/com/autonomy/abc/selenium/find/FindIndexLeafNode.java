package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.element.Checkbox;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

class FindIndexLeafNode implements IndexNodeElement {
    private Checkbox checkbox;
    private WebElement container;

    FindIndexLeafNode(WebElement element, WebDriver driver) {
        container = element;
        checkbox = new FindIndexCheckbox(element, driver);
    }

    @Override
    public void select() {
        checkbox.check();
    }

    @Override
    public void deselect() {
        checkbox.uncheck();
    }

    private WebElement selector() {
        return container.findElement(By.cssSelector(".category-name, .database-name"));
    }

    @Override
    public boolean isSelected() {
        return ElementUtil.hasClass("icon-ok", container.findElement(By.cssSelector(".database-icon")));
    }

    private boolean isPartiallySelected() {
        return ElementUtil.hasClass("icon-minus", container.findElement(By.cssSelector(".database-icon")));
    }

    @Override
    public String getName() {
        return container.findElement(By.className("database-name")).getText();
    }
}
