package com.autonomy.abc.selenium.find.numericWidgets;

import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NumericWidgetService {

    private final BIIdolFindElementFactory elementFactory;
    private final BIIdolFind find;
    private IdolFindPage findPage;

    public NumericWidgetService(final BIIdolFind find) {
        elementFactory = find.elementFactory();
        this.find = find;
        findPage = elementFactory.getFindPage();
    }

    public MainNumericWidget waitForReload() {
        elementFactory.getFilterPanel().waitForParametricFields();
        MainNumericWidget mainGraph = findPage.mainGraph();
        mainGraph.waitUntilWidgetLoaded();
        return mainGraph;
    }

    public String selectFilterGraph(GraphFilterContainer container, final WebDriver driver) {
        container.expand();
        String graphTitle = container.filterCategoryName();
        new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean> (){
            @Override
            public Boolean apply(final WebDriver driver) {
                return !container.isCollapsed();
            }
        });
        container.graph().click();
        return graphTitle;
    }

    public MainNumericWidget searchAndSelectNthGraph(int n, String searchTerm, final WebDriver driver) {
        IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getNthGraph(n), driver);

        return findPage.mainGraph();
    }

    public MainNumericWidget searchAndSelectFirstNumericGraph(final String searchTerm, final WebDriver driver) {
        IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getFirstNumericGraph(), driver);

        return findPage.mainGraph();
    }

    public MainNumericWidget searchAndSelectFirstDateGraph(final String searchTerm, final WebDriver driver) {
        IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getFirstDateGraph(), driver);
        return findPage.mainGraph();
    }

    private IdolFilterPanel searchAndReturnFilterPanel(String searchTerm){
        find.findService().searchAnyView(searchTerm);
        final IdolFilterPanel filterPanel = elementFactory.getFilterPanel();
        filterPanel.waitForParametricFields();
        return filterPanel;
    }


}
