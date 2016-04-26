package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.autonomy.abc.selenium.find.SimilarDocumentsView;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import org.openqa.selenium.WebDriver;

public abstract class FindElementFactory extends ElementFactoryBase {
    protected FindElementFactory(WebDriver driver, PageMapper<?> mapper) {
        super(driver, mapper);
    }

    public abstract FindPage getFindPage();
    public abstract FindTopNavBar getTopNavBar();
    public abstract FindResultsPage getResultsPage();
    public abstract SimilarDocumentsView getSimilarDocumentsView();
}
