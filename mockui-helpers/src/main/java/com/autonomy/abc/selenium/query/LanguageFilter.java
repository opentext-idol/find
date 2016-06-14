package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.language.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageFilter implements QueryFilter {
    private final Language language;
    private final Logger logger = LoggerFactory.getLogger(LanguageFilter.class);

    public LanguageFilter(String language) {
        this.language = Language.fromString(language);
    }

    public LanguageFilter(Language language) {
        this.language = language;
    }

    // TODO: create via app-specific factory
    @Override
    public void apply(QueryFilter.Filterable page) {
        if (page instanceof LanguageFilter.Filterable) {
            try {
                ((LanguageFilter.Filterable) page).selectLanguage(language);
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

    public interface Filterable extends QueryFilter.Filterable {
        void selectLanguage(Language language);
    }
}
