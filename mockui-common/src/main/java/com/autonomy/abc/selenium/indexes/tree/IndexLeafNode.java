package com.autonomy.abc.selenium.indexes.tree;

import com.autonomy.abc.selenium.element.SOCheckbox;
import com.hp.autonomy.frontend.selenium.element.Checkbox;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IndexLeafNode implements IndexNodeElement {
    private final Checkbox checkbox;
    private final WebElement container;

    public IndexLeafNode(WebElement element, WebDriver driver) {
        checkbox = new SOCheckbox(element, driver);
        container = element;
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
        return container.getText();
    }
}
