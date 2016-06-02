package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindResultsSunburst;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;

public class SunburstITCase extends IdolFindTestBase {
    private IdolFindPage findPage;
    private FindResultsSunburst results;
    private FindService findService;

    public SunburstITCase(TestConfig config){super(config);}

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        results = findPage.getSunburst();
        findService = getApplication().findService();
    }

    @Test
    public void testSunburstTabShowsSunburst(){
        findService.search("shambolic");
        results.goToSunburst();

        verifyThat("Main results list hidden",results.mainResultsContainerHidden());
        verifyThat("Sunburst element displayed",results.sunburstVisible());
    }

    @Test
    public void testParametricSelectors(){
        findService.search("wild horses");
        results.goToSunburst();

        String firstParametric = findPage.get1stParametricFilterTypeName();
        verifyThat("Default parametric selection is 1st parametric type",firstParametric,equalToIgnoringCase(results.nthParametricFilterName(1)));

        results.parametricSelectionDropdown(2).open();
        verifyThat("1st selected parametric does not appear as choice in 2nd",results.getParametricDropdownItems(2),not(contains(firstParametric)));
    }

    @Test
    public void testParametricSelectorsChangeDisplay(){
        findService.search("cricket");
        results.goToSunburst();
        results.parametricSelectionDropdown(1).select("SOURCE");
        Waits.loadOrFadeWait();

        findPage.showFilters();
        int correctNumberSegments = findPage.numParametricChildrenBigEnoughForSunburst("SOURCE");
        assertThat("Correct number ("+correctNumberSegments+") of sunburst segments ",results.numberOfSunburstSegments(),is(correctNumberSegments));
    }

    @Test
    public void testHoveringOverSegmentCausesTextToChange(){
        findService.search("elephant");
        results.goToSunburst();

        findPage.showFilters();
        List<String> bigEnough = findPage.nameParametricChildrenBigEnoughForSunburst("CATEGORY");

        for(WebElement segment:results.findSunburstSegments()){
            //Only works if elements on RHS of circle
            results.hoveringRight(segment);
            String name = results.getSunburstCentreName();
            verifyThat("Hovering gives message in centre of sunburst",name,not(""));
            verifyThat("Name is correct - "+name,name,isIn(bigEnough));
        }
        results.hoverOverTooFewToDisplaySegment();

        verifyThat("Hovering on greyed segment explains why grey",results.getSunburstCentreName(),allOf(containsString("Please refine your search"),containsString("too small to display")));


    }
    @Test
    public void testClickingSunburstSegmentFiltersTheSearch(){
        //needs to search something that only has 2 parametric filter types
        findService.search("churchill");
        results.goToSunburst();
        results.parametricSelectionDropdown(1).select("CATEGORY");

        String filterBy = results.hoverOnSegmentGetCentre(1);
        String parametricSelectionName = results.nthParametricFilterName(1);
        results.getIthSunburstSegment(1).click();

        verifyThat("The correct filter label has appeared: "+filterBy,ElementUtil.getTexts(findPage.filterLabels()),contains(equalToIgnoringCase(filterBy)));


        assertThat("Parametric selection name "+parametricSelectionName,1,is(1));
        verifyThat("Side bar shows only "+filterBy,findPage.numberOfParametricFilterChildren(parametricSelectionName),is(1));
        verifyThat("Parametric selection name has changed to another type of filter",results.nthParametricFilterName(1),not(is(parametricSelectionName)));

        results.getIthSunburstSegment(1);
        //TODO: wait til desired behaviour known for when runs out of parametric filter types
        //verifyThat("When run out of parametric types to filter by ",,);

    }

    @Test
    public void testSideBarFiltersChangeSunburst(){
        findService.search("lashing");
        results.goToSunburst();

        String parametricSelectionFirst= results.nthParametricFilterName(1);
        findPage.firstChildOfFirstParametricType().click();

        results.waitForSunburst();
        assertThat("Parametric selection changed",results.nthParametricFilterName(1),not(is(parametricSelectionFirst)));
    }

    @Test
    public void testTwoParametricSelectorSunburst(){
        findService.search("rushdie");
        results.goToSunburst();

        results.parametricSelectionDropdown(1).select("PERSON");
        results.waitForSunburst();
        int segNumberBefore = results.numberOfSunburstSegments();

        results.parametricSelectionDropdown(2).select("PLACE");
        results.waitForSunburst();
        int segNumberAfter = results.numberOfSunburstSegments();

        assertThat("More segments with second parametric selector",segNumberAfter,greaterThan(segNumberBefore));
    }




}
