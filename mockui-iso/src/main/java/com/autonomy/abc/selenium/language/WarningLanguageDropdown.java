package com.autonomy.abc.selenium.language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarningLanguageDropdown implements LanguageDropdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarningLanguageDropdown.class);

    @Override
    public void open() {
        LOGGER.warn("cannot open language dropdown");
    }

    @Override
    public void close() {}

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public Language getSelected() {
        return Language.ENGLISH;
    }

    @Override
    public void select(final Language language) {
        LOGGER.warn("cannot select language");
    }

}
