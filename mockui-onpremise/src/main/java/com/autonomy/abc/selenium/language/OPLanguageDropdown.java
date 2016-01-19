package com.autonomy.abc.selenium.language;

import com.autonomy.abc.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OPLanguageDropdown implements LanguageDropdown {
    private Dropdown dropdown;

    public OPLanguageDropdown(AppElement element) {
        dropdown = new Dropdown(element);
    }

    public OPLanguageDropdown(WebElement element, WebDriver driver) {
        this(new AppElement(element, driver));
    }

    @Override
    public void open() {
        dropdown.open();
    }

    @Override
    public void close() {
        dropdown.close();
    }

    @Override
    public boolean isOpen() {
        return dropdown.isOpen();
    }

    @Override
    public Language getSelected() {
        return Language.fromString(dropdown.getValue());
    }

    @Override
    public void select(Language language) {
        if (getSelected() != language) {
            dropdown.select(language.toString());
        }
    }
}
