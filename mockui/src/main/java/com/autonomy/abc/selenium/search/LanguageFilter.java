package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.page.search.SearchBase;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageFilter implements SearchFilter {
    private String language;
    private Logger logger = LoggerFactory.getLogger(LanguageFilter.class);

    public LanguageFilter(String language) {
        this.language = language;
    }

    // TODO: create via app-specific factory
    @Override
    public void apply(SearchBase searchBase) {
        try {
            searchBase.languageButton().click();
            searchBase.javascriptClick(searchBase.findElement(By.className("search-language")).findElement(By.linkText(language)));
        } catch (Exception e) {
            logger.info("language not found");
        }
    }
}
