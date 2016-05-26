package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.find.*;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;

public class SunburstITCase extends FindTestBase {
    private IdolFindPage findPage;
    private FindResultsSunburst results;
    private FindService findService;

    public SunburstITCase(TestConfig config){super(config);}

    @Before
    public void setUp(){
        findPage = getElementFactory().getIdolFindPage();
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
        results.parametricSelectionDropdown(1).select("OVERALL_VIBE");
        Waits.loadOrFadeWait();

        int correctNumberSegments = findPage.numberOfParametricFilterChildren("OVERALL VIBE");
        assertThat("Correct number ("+correctNumberSegments+") of sunburst segments ",results.numberOfSunburstSegments(),is(correctNumberSegments));
    }



}
