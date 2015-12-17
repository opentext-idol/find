package com.autonomy.abc.selenium.indexes.tree;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FindIndexLeafNode implements IndexNodeElement {
    private WebElement container;

    FindIndexLeafNode(WebElement clickable) {
        container = clickable;
    }

    @Override
    public void select() {
        if (isPartiallySelected()) {
            selector().click();
            selector().click();
        } else if (!isSelected()) {
            selector().click();
        }
    }

    @Override
    public void deselect() {
        if (isSelected() || isPartiallySelected()) {
            selector().click();
        }
    }

    private WebElement selector() {
        return container.findElement(By.cssSelector(".category-name, .database-name"));
    }

    @Override
    public boolean isSelected() {
        return AppElement.hasClass("icon-ok", container.findElement(By.cssSelector(".database-icon")));
    }

    private boolean isPartiallySelected() {
        return AppElement.hasClass("icon-minus", container.findElement(By.cssSelector(".database-icon")));
    }

    @Override
    public String getName() {
        return container.findElement(By.className("database-name")).getText();
    }
}
