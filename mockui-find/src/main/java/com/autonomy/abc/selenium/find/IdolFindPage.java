package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class IdolFindPage extends FindPage {

    private IdolFindPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected FilterPanel filters() {
        return new FilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        public IdolFindPage create(WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}

