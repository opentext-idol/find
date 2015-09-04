package com.autonomy.abc.selenium.page.keywords;

import org.openqa.selenium.WebDriver;
import org.slf4j.LoggerFactory;

public class HSOCreateNewKeywordsPage extends CreateNewKeywordsPage {

    public HSOCreateNewKeywordsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void selectLanguage(String language) {
        LoggerFactory.getLogger(HSOCreateNewKeywordsPage.class).warn("Cannot select language for Hosted");
        return;
    }
}
