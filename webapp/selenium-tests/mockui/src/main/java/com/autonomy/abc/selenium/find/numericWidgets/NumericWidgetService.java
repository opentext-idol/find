package com.autonomy.abc.selenium.find.numericWidgets;

import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.IdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;

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

    public String selectFilterGraph(GraphFilterContainer container) {
        container.expand();
        String graphTitle = container.filterCategoryName();
        container.graph().click();
        return graphTitle;
    }

    public MainNumericWidget searchAndSelectNthGraph(int n, String searchTerm) {
        IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getNthGraph(n));

        return findPage.mainGraph();
    }

    public MainNumericWidget searchAndSelectFirstNumericGraph(final String searchTerm) {
        IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getFirstNumericGraph());

        return findPage.mainGraph();
    }

    public MainNumericWidget searchAndSelectFirstDateGraph(final String searchTerm) {
        IdolFilterPanel filterPanel = searchAndReturnFilterPanel(searchTerm);
        selectFilterGraph(filterPanel.getFirstDateGraph());

        return findPage.mainGraph();
    }

    private IdolFilterPanel searchAndReturnFilterPanel(String searchTerm){
        find.findService().searchAnyView(searchTerm);
        final IdolFilterPanel filterPanel = elementFactory.getFilterPanel();
        filterPanel.waitForParametricFields();
        return filterPanel;
    }


}
