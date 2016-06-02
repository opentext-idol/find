package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.hp.autonomy.frontend.selenium.element.Checkbox;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

class FindIndexLeafNode implements IndexNodeElement {
    private final Checkbox checkbox;
    private final WebElement container;

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

    @Override
    public boolean isSelected() {
        return ElementUtil.hasClass("icon-ok", container.findElement(By.cssSelector(".database-icon")));
    }

    @Override
    public String getName() {
        return container.findElement(By.className("database-name")).getText();
    }
}
