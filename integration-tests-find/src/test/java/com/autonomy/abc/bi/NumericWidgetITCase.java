package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;

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
    public void clickAndDrag(){
        findService.search("face");

        filters().waitForParametricFields();



        MainNumericWidget mainGraph = findPage.mainGraph();

        //+x -> goes right on the x axis
        mainGraph.clickAndDrag(100,0,mainGraph.graph());
    }
    @Test
    public void testClickingOnFilterPanelGraphOpensMain(){
        //default main graph not present
        //go thru all filter panel graphs and get titles then click.
        //Check there is a main graph and the main graph has the right title
        //close main
        //check it's gone
    }

    @Test
    @ResolvedBug("FIND-356")
    public void testSelectionRecDoesNotDisappear(){
        //make selection
        //check it's still there
    }

    @Test
    @ResolvedBug("FIND-365")
    public void testFilterLabelFormatReflectsNumericData(){
        //the filter label shouldn't always be the date format
        //e.g. if data is elevation
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
        //apply rec
        //check label
        //move same rectangle
        //check label is different
    }

    @Test
    @ResolvedBug("FIND-282")
    public void testFilterLabelsHaveTitle(){
        //test the filter labels have title e.g. "AUTN Date:
    }

    //FIND-323 -> zoom in zoom out
    //FIND-304 -> resizing
    //FIND-389   //INteraction w/ saved search

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
    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
