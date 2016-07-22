package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.filters.DateOption;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.autonomy.abc.selenium.find.numericWidgets.NumericWidget;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.SortBy;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

//FIND-323 -> zoom in zoom out
//FIND-304 -> resizing
//FIND-389   //INteraction w/ saved search


public class NumericWidgetITCase extends IdolFindTestBase{
    private FindService findService;
    private IdolFindPage findPage;

    public NumericWidgetITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }

    private MainNumericWidget waitForReload(){
        filters().waitForParametricFields();
        MainNumericWidget mainGraph = findPage.mainGraph();
        mainGraph.waitUntilWidgetLoaded();
        return mainGraph;
    }

    private String selectFilterGraph(GraphFilterContainer container){
        container.expand();
        String graphTitle = container.getParentName();
        container.graph().click();
        return graphTitle;
    }

    private MainNumericWidget searchAndSelectNthGraph(int n, String searchTerm){
        findService.search(searchTerm);
        IdolFilterPanel filterPanel = filters();
        filterPanel.waitForParametricFields();

        selectFilterGraph(filterPanel.getNthGraph(n));

        return findPage.mainGraph();
    }

    @Test
    public void testClickingOnFilterPanelGraphOpensMain(){
        findService.search("book");
        filters().waitForParametricFields();

        assertThat("Default: main graph not shown",!findPage.mainGraphDisplayed());

        MainNumericWidget mainGraph;
        for(GraphFilterContainer container : filters().graphContainers()){
            String graphTitle = selectFilterGraph(container);
            verifyThat("Main graph now shown",findPage.mainGraphDisplayed());

            mainGraph = findPage.mainGraph();
            //doesn't actually check contents!
            verifyThat("Correct graph is open",mainGraph.header(),equalToIgnoringCase(graphTitle));
        }

        findPage.mainGraph().closeWidget();
        verifyThat("Main graph now gone",!findPage.mainGraphDisplayed());
    }



    @Test
    @ResolvedBug("FIND-356")
    public void testSelectionRecDoesNotDisappear(){

        MainNumericWidget mainGraph = searchAndSelectNthGraph(0,"politics");

        mainGraph.clickAndDrag(100,0,mainGraph.graph());

        filters().waitForParametricFields();
        mainGraph.waitUntilWidgetLoaded();

        verifyThat("Selection rectangle hasn't disappeared",mainGraph.graphAsWidget().selectionRectangleExists());
    }


    @Test
    public void testSelectionRecFiltersResults(){
        MainNumericWidget mainGraph = searchAndSelectNthGraph(1,"space");
        int beforeParametricFilters = filters().numberParametricFieldContainers();
        int beforeNumberResults = findPage.totalResultsNum();
        mainGraph.waitUntilWidgetLoaded();

        mainGraph.selectHalfTheBars();
        mainGraph = waitForReload();

        verifyThat("Filter label has appeared",findPage.getFilterLabels(),hasSize(1));
        verifyThat("Fewer parametric filters",filters().numberParametricFieldContainers(),lessThan(beforeParametricFilters));
        verifyThat("Fewer results",findPage.totalResultsNum(),lessThan(beforeNumberResults));

        NumericWidget sidePanelChart = filters().getNthGraph(1).getChart();
        verifyThat("Side panel chart has selection rectangle",sidePanelChart.selectionRectangleExists());

        mainGraph.reset();
        mainGraph = waitForReload();

        verifyThat("Selection rectangle gone from centre",!mainGraph.graphAsWidget().selectionRectangleExists());
        sidePanelChart = filters().getNthGraph(1).getChart();
        verifyThat("Selection rectangle gone from side panel",!sidePanelChart.selectionRectangleExists());
    }



    @Test
    @ActiveBug("FIND-392")
    public void testWidgetsReflectCurrentSearch(){
        MainNumericWidget mainGraph = searchAndSelectNthGraph(2,"face");
        mainGraph.selectFractionOfBars(3,4);
        waitForReload();
        verifyThat("There are results present",findPage.totalResultsNum(),greaterThan(0));
    }


    @Test
    public void testSelectionRecFiltersResultsCorrectly(){
        //test hovering over the bars -> something sensible on bottom
        //test it's never negative

        //FIND-
        //



        //test things like range of data
        //max, min
        //mathsy things
    }



    @Test
    @ActiveBug("FIND-336")
    public void testZoomingOutFar(){
        //test doesn't crash if zoom out really far
        /*MainNumericWidget mainGraph = searchAndSelectNthGraph(1,"politics");

        mainGraph.graph().click();

        mainGraph.simulateZoomingIn();*/


        //need to check the little text boxes are changing
        //need to check the bottom date when you hover over the bar is sensible -> not negative
    }

    @Test
    @ActiveBug("FIND-300")
    public void testZooming() {
        //test range of purple remains the same
        //the selection rec moves independently of x axis -> range changes

        //Applies zooming to side panel
    }


    @Test
    public void testMinAndMaxReflectCurrentSearch(){
        //currently 0th graph is place elevation (i.e. non-date)
        searchAndSelectNthGraph(0,"*");
        checkBoundsForPlaceElevationWidget();

        searchAndSelectNthGraph(1,"moon");
        checkBoundsForDateWidget();

        //convert to same format as the boxes

        //check that are the same as the value in the boxes minus the actual time part
        //
    }

    //bad/flakey but there's no other way to do it right now
    private void checkBoundsForPlaceElevationWidget(){
        findPage.filterBy(new IndexFilter("Cities"));
        MainNumericWidget mainGraph=findPage.mainGraph();
        final int originalRange = getRange(mainGraph);

        findService.search("Tse");
        mainGraph = findPage.mainGraph();
        final int newRange = getRange(mainGraph);

        verifyThat("The bounds for the graphs are determined by the current query",newRange,lessThan(originalRange));
    }

    private int getRange(MainNumericWidget mainGraph){
        return Integer.parseInt(mainGraph.maxNumValue()) - Integer.parseInt(mainGraph.minNumValue());
    }

    private void checkBoundsForDateWidget(){
        findService.search("moon");
        findPage.filterBy(IndexFilter.ALL);
        findPage.sortBy(SortBy.DATE);
        ResultsView results = getElementFactory().getResultsPage();
        String firstResultDate = results.getResult(1).convertDate();

        for(int i=0;i<10;i++) {
            findPage.scrollToBottom();
        }

        String lastResultDate = results.getResult(results.getResultsCount()).convertDate();
    }

    @Test
    @ActiveBug("FIND-390")
    public void testInteractionWithRegularDateFilters(){
        MainNumericWidget mainGraph = searchAndSelectNthGraph(0,"whatever");
        filters().toggleFilter(DateOption.MONTH);

        filters().waitForParametricFields();
        mainGraph.waitUntilWidgetLoaded();

        WebElement errorMessage = mainGraph.errorMessage();
        verifyThat("Error message not displayed",!errorMessage.isDisplayed());
        verifyThat("Error message not 'failed to load data'",errorMessage.getText(),not(equalToIgnoringCase("Failed to load data")));
    }

    @Test
    @ResolvedBug("FIND-366")
    public void testFilterLabelsUpdate(){
        findService.search("dance");
        filters().waitForParametricFields();
        selectFilterGraph(filters().getNthGraph(0));

        MainNumericWidget mainGraph = findPage.mainGraph();
        mainGraph.clickAndDrag(100,0,mainGraph.graph());
        String label = findPage.getFilterLabels().get(0);

        mainGraph.clickAndDrag(-100,0,mainGraph.graph());
        String changedLabel = findPage.getFilterLabels().get(0);
        assertThat("The label has changed",changedLabel,not(is(label)));
    }

    @Test
    @ResolvedBug("FIND-282")
    public void testFilterLabelsHaveTitle(){
        findService.search("ball");
        filters().waitForParametricFields();

        List<String> graphTitles = filterByAllGraphs();
        List<String> labels = findPage.getFilterLabels();

        verifyThat("All filters have a label",labels,hasSize(graphTitles.size()));

        for(int i=0;i<graphTitles.size();i++){
            String title = graphTitles.get(i);
            verifyThat("Title "+title+" is in filter label",labels.get(i),containsString(title));
        }
    }

    private List<String> filterByAllGraphs(){
        List<String> titles = new ArrayList<>();
        MainNumericWidget mainGraph;

        for(GraphFilterContainer container : filters().graphContainers()) {
            titles.add(selectFilterGraph(container));
            mainGraph = findPage.mainGraph();
            mainGraph.clickAndDrag(100,0,mainGraph.graph());
        }

        return titles;
    }

    @Test
    @ResolvedBug("FIND-365")
    //horrendously fragile -> v dependent on specific filters
    public void testFilterLabelFormatReflectsNumericData(){
        MainNumericWidget mainGraph = searchAndSelectNthGraph(0,"beer");
        assumeThat("Test assumes that 0th graph is place elevation",mainGraph.header(),equalToIgnoringCase("Place Elevation"));
        mainGraph.clickAndDrag(200,0,mainGraph.graph());
        waitForReload();
        String firstLabel =findPage.getFilterLabels().get(0).split(":")[1];
        verifyThat("Place elevation filter label doesn't have time format",firstLabel,not(containsString(":")));

    }

    @Test
    public void testInputBoundsAsText(){
        //should actually change the search
        //need to be careful of some graph being date and others not being
    }

    @Test
    public void testInputDateBoundsWithCalendar(){
        //need to get just the widgets with date
        //check if it has date in the name it has calendar button
        //BUT can't assume that because it doesn't it isn't date
        //check changes search
    }

    //SHOULD THIS BE IN HERE OR SHOULD IT BE IN IDOLFINDPAGE?!
    private IdolFilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
