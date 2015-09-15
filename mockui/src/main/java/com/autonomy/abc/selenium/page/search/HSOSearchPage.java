package com.autonomy.abc.selenium.page.search;

import org.openqa.selenium.WebDriver;
import org.slf4j.LoggerFactory;

public class HSOSearchPage extends SearchPage {

    public HSOSearchPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    public void selectLanguage(final String language, final String type) {
        LoggerFactory.getLogger(HSOSearchPage.class).warn("Cannot select language on hosted yet");
    }
}
