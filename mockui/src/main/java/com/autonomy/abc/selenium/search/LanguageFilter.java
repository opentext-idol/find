package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageFilter implements SearchFilter {
    private Language language;
    private Logger logger = LoggerFactory.getLogger(LanguageFilter.class);

    public LanguageFilter(String language) {
        this.language = Language.fromString(language);
    }

    public LanguageFilter(Language language) {
        this.language = language;
    }

    // TODO: create via app-specific factory
    @Override
    public void apply(SearchFilter.Filterable page) {
        if (page instanceof SearchPage) {
            try {
                ((SearchPage) page).selectLanguage(language);
            } catch (Exception e) {
                logger.warn("language not found");
            }
        } else {
            logger.warn("languages do not appear on " + page.getClass());
        }
    }

    @Override
    public String toString() {
        return "LanguageFilter:" + language;
    }
}
