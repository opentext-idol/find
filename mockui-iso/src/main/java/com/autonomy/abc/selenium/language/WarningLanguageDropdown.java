package com.autonomy.abc.selenium.language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarningLanguageDropdown implements LanguageDropdown {
    private final static Logger LOGGER = LoggerFactory.getLogger(WarningLanguageDropdown.class);

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

    public void select(final Language language) {
        LOGGER.warn("cannot select language");
    }

}
