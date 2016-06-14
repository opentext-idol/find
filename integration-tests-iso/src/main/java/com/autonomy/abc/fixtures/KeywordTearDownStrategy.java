package com.autonomy.abc.fixtures;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.keywords.KeywordFilter;

public class KeywordTearDownStrategy extends IsoTearDownStrategyBase {
    @Override
    public void cleanUpApp(final IsoApplication<?> app) {
        app.keywordService().deleteAll(KeywordFilter.ALL);
    }
}
