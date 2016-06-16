package com.autonomy.abc.selenium.language;

import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolLanguageDropdown implements LanguageDropdown {
    private final Dropdown dropdown;

    public IdolLanguageDropdown(final AppElement element) {
        dropdown = new Dropdown(element);
    }

    public IdolLanguageDropdown(final WebElement element, final WebDriver driver) {
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
    public void select(final Language language) {
        if (getSelected() != language) {
            dropdown.select(language.toString());
        }
    }
}
