package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.FindParametricCheckbox;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public class IdolFindPage extends FindPage {

    private IdolFindPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected FilterPanel filters() {
        return new FilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    /**
     * Determines which values for a parametric field are significant 
     * enough to be display in sunburst
     * @param container a parametric field
     * @return the significant values
     */
    public List<String> getSunburstableValuesFor(ParametricFieldContainer container) {
        List<String> names = new ArrayList<>();
        for (FindParametricCheckbox checkbox : sunburstableValues(container)) {
            names.add(checkbox.getName());
        }
        return names;
    }

    private List<FindParametricCheckbox> sunburstableValues(ParametricFieldContainer container) {
        return FindResultsSunburst.expectedParametricValues(container.values());
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        public IdolFindPage create(WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}

