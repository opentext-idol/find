package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
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

    private String selectFilterGraph(GraphFilterContainer container){
        container.expand();
        String graphTitle = container.getParentName();
        container.graph().click();
        return graphTitle;
    }

    @Test
    @ResolvedBug("FIND-356")
    public void testSelectionRecDoesNotDisappear(){
        findService.search("politics");
        IdolFilterPanel filterPanel = filters();
        filterPanel.waitForParametricFields();

        selectFilterGraph(filterPanel.getNthGraph(0));

        MainNumericWidget mainGraph = findPage.mainGraph();
        mainGraph.clickAndDrag(100,0,mainGraph.graph());

        filterPanel.waitForParametricFields();
        mainGraph.waitUntilWidgetLoaded();

        verifyThat("Selection rectangle hasn't disappeared",mainGraph.selectionRectangleExists());
    }


    @Test
    public void testSelectionRecFiltersResults(){
        //use rectangle on main
        //should reload search
        //filter appears at top
        //side panel changes
        //no. of results <= original
        //purple box should have appeared in the side panel
        //check reset button
    }

    @Test
    public void testSelectionRecFiltersResultsCorrectly(){
        //test things like range of data
        //max, min
        //mathsy things
    }

    @Test
    @ActiveBug("FIND-336")
    public void testZoomingOutFar(){
        //test doesn't crash if zoom out really far

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
        //values for noMin and noMax bounds should be for current search
        //NOT whole data set

    }

    @Test
    @ActiveBug("FIND-390")
    public void testInteractionWithRegularDateFilters(){
        //Message of "Failed to load data" - shouldn't be there
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
    public void testFilterLabelFormatReflectsNumericData(){
        //the filter label shouldn't always be the date format
        //e.g. if data is elevation
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
