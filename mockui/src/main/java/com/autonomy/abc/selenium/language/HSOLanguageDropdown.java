package com.autonomy.abc.selenium.language;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HSOLanguageDropdown implements LanguageDropdown {
    private final static Logger LOGGER = LoggerFactory.getLogger(HSOLanguageDropdown.class);

    public HSOLanguageDropdown(WebElement element) {}

    public void open() {
        LOGGER.warn("cannot open language dropdown");
    }

    public void close() {}

    public boolean isOpen() {
        return false;
    }

    public Language getSelected() {
        return Language.ENGLISH;
    }

    public void select(Language language) {
        LOGGER.warn("cannot select language");
    }

}
