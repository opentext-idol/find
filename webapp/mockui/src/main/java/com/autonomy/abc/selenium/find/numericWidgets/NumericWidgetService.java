package com.autonomy.abc.selenium.find.numericWidgets;

import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.IdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;

public class NumericWidgetService {

    private final IdolFindElementFactory elementFactory;
    private final IdolFind find;
    private IdolFindPage findPage;

    public NumericWidgetService(final IdolFind find) {
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

    public String selectFilterGraph(GraphFilterContainer container) {
        container.expand();
        String graphTitle = container.filterCategoryName();
        container.graph().click();
        return graphTitle;
    }

    public MainNumericWidget searchAndSelectNthGraph(int n, String searchTerm) {
        find.findService().search(searchTerm);
        IdolFilterPanel filterPanel = elementFactory.getFilterPanel();
        filterPanel.waitForParametricFields();

        selectFilterGraph(filterPanel.getNthGraph(n));

        return findPage.mainGraph();
    }


}
