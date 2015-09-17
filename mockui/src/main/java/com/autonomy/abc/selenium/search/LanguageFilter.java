package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.page.search.SearchPage;
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
    public void apply(SearchPage searchPage) {
        logger.info("skipping language filter on hosted");
//        searchPage.languageButton().click();
//        searchPage.javascriptClick(searchPage.findElement(By.className("search-language")).findElement(By.linkText(language)));
    }
}
