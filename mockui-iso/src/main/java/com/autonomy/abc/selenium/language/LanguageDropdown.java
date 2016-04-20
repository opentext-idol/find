package com.autonomy.abc.selenium.language;

public interface LanguageDropdown {
    void open();
    void close();
    boolean isOpen();

    Language getSelected();
    void select(Language language);
}
