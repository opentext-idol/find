package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.ParametricFilterModal;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.checked;
import static org.hamcrest.Matchers.*;

public class SunburstITCase extends IdolFindTestBase {
    private IdolFindPage findPage;
    private SunburstView results;
    private FindService findService;

    public SunburstITCase(final TestConfig config){super(config);}

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        results = getElementFactory().getSunburst();
        findService = getApplication().findService();
    }

    //TODO: test that checks the total doc number against what's in sunburst centre
    //TODO: test that checks what happens to sunburst when docs have 2 (non-mutually exclusive) fields from the same category

    @Test
    @ActiveBug("FIND-382")
    public void testSunburstTabShowsSunburstOrMessage(){
        search("shambolic");

        verifyThat("Main results list hidden",getElementFactory().getResultsPage().mainResultsContainerHidden());
        verifyThat("Sunburst element displayed",results.sunburstVisible());
        verifyThat("Parametric selectors appear",results.parametricSelectionDropdownsExist());

        search("shouldBeNoFieldsForThisCrazySearch");

        //TODO: find out expected behaviour once bug is resolved
        //verifyThat("Sensible message is appearing when there is no sunburst")
    }

    @Test
    @ResolvedBug("FIND-405")
    public void testParametricSelectors(){
        search("wild horses");

        final String firstParametric = filters().parametricField(0).getParentName();
        verifyThat("Default parametric selection is 1st parametric type", firstParametric, startsWith(results.getSelectedFieldName(1).toUpperCase()));

        results.parametricSelectionDropdown(2).open();
        verifyThat("1st selected parametric does not appear as choice in 2nd",results.getParametricDropdownItems(2),not(contains(firstParametric)));
    }

    @Test
    public void testParametricSelectorsChangeDisplay(){
        search("cricket");

        //only works if you have at least 2 parametric types
        results.parametricSelectionDropdown(1).selectItem(1);
        Waits.loadOrFadeWait();

        int correctNumberSegments = getFilterResultsBigEnoughToDisplay(1).size();

        assertThat("Correct number ("+correctNumberSegments+") of sunburst segments ",results.numberOfSunburstSegments(),is(correctNumberSegments));
    }

    @Test
    public void testHoveringOverSegmentCausesTextToChange(){
        search("elephant");

        List<String> bigEnough = getFilterResultsBigEnoughToDisplay(0);

        for (final WebElement segment : results.findSunburstSegments()) {
            results.segmentHover(segment);
            final String name = results.getSunburstCentreName();
            verifyThat(name, not(isEmptyOrNullString()));
            verifyThat(name, isIn(bigEnough));
        }
    }

    //TODO: seeAll should create a modal!!!!
    private List<String> getFilterResultsBigEnoughToDisplay(int filterContainerIndex){
        filters().parametricField(filterContainerIndex).expand();
        filters().parametricField(filterContainerIndex).seeAll();

        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        final List<String> bigEnough = filterModal.expectedParametricValues();
        filterModal.cancel();

        findPage.waitUntilParametricModalGone();
        return bigEnough;
    }

    @Test
    public void testHoveringOnGreySegmentGivesMessage(){
        search("elephant");

        assumeThat("Some segments not displayable",results.greySunburstAreaExists());
        results.hoverOverTooFewToDisplaySegment();

        verifyThat("Hovering on greyed segment explains why grey",results.getSunburstCentreName(),allOf(containsString("Please refine your search"),containsString("too small to display")));
    }

    @Test
    public void testClickingSunburstSegmentFiltersTheSearch(){
        //needs to search something that only has 2 parametric filter types
        search("churchill");

        final String fieldValue = results.hoverOnSegmentGetCentre(1);
        final String fieldName = results.getSelectedFieldName(1);
        LOGGER.info("filtering by " + fieldName + " = " + fieldValue);
        results.getIthSunburstSegment(1).click();
        results.waitForSunburst();
        
        verifyThat(findPage.getFilterLabels(), hasItem(containsString(fieldValue)));
        filters().expandParametricContainer(fieldName);
        verifyThat(filters().checkboxForParametricValue(fieldName, fieldValue), checked());
        verifyThat("Parametric selection name has changed to another type of filter",results.getSelectedFieldName(1),not(fieldName));

    }

    @Test
    @ResolvedBug("FIND-379")
    public void testSideBarFiltersChangeSunburst(){
        search("lashing");

        final String parametricSelectionFirst= results.getSelectedFieldName(1);
        filters().parametricField(0).expand();
        filters().checkboxForParametricValue(0, 0).check();

        results.waitForSunburst();
        assertThat("Parametric selection changed",results.getSelectedFieldName(1),not(is(parametricSelectionFirst)));
    }

    //will probably fail if your databases are different to the testing ones
    @Test
    public void testTwoParametricSelectorSunburst(){
        search("cameron");

        results.parametricSelectionDropdown(1).select("Category");
        results.waitForSunburst();
        final int segNumberBefore = results.numberOfSunburstSegments();

        results.parametricSelectionDropdown(2).select("Source");
        results.waitForSunburst();
        final int segNumberAfter = results.numberOfSunburstSegments();

        assertThat("More segments with second parametric selector",segNumberAfter,greaterThan(segNumberBefore));
    }

    private void search(String searchTerm) {
        findService.search(searchTerm);
        findPage.goToSunburst();
        results.waitForSunburst();
    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
