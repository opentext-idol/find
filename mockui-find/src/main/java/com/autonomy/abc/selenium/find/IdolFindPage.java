package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IdolFindPage extends FindPage {

    private IdolFindPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    protected FilterPanel filters() {
        return new FilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    public ComparisonModal openCompareModal() {
        compareButton().click();
        return ComparisonModal.make(getDriver());
    }

    public WebElement compareButton() {
        return mainContainer().findElement(By.className("compare-modal-button"));
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        @Override
        public IdolFindPage create(final WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}
