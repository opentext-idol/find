package com.autonomy.abc.selenium.find.filters;

import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.hp.autonomy.frontend.selenium.element.CheckboxBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

class FindIndexLeafNode implements IndexNodeElement {
    private final CheckboxBase checkbox;
    private final WebElement container;

    FindIndexLeafNode(final WebElement element) {
        container = element;
        checkbox = new FindIndexCheckbox(element.findElement(By.tagName("span")));
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
        return checkbox.isChecked();
    }

    @Override
    public String getName() {
        return container.findElement(By.className("database-name")).getText();
    }
}
