package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
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
import static org.openqa.selenium.lift.Matchers.displayed;

@Role(UserRole.BIFHI)
public class SunburstITCase extends IdolFindTestBase {
    private IdolFindPage findPage;
    private SunburstView results;
    private FindService findService;
    private ConceptsPanel conceptsPanel;

    public SunburstITCase(final TestConfig config) {
        super(config);
    }

    //TODO HAVE CHANGED getSunburst() IN ORDER TO GET THE ACTIVE THING
    //NEED TO LOOK AT THE CONTAINER STUFF
    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        results = ((BIIdolFindElementFactory) getElementFactory()).getSunburst();
        findService = getApplication().findService();
        conceptsPanel = getElementFactory().getConceptsPanel();
    }

    //TODO: test that checks the total doc number against what's in sunburst centre
    //TODO: test that checks what happens to sunburst when docs have 2 (non-mutually exclusive) fields from the same category

    @Test
    @ResolvedBug("FIND-251")
    @ActiveBug("FIND-382")
    public void testSunburstTabShowsSunburstOrMessage() {
        search("shambolic");

        results.waitForSunburst();

        verifyThat("Sunburst element displayed", results.sunburstVisible());
        verifyThat("Parametric selectors appear", results.parametricSelectionDropdownsExist());

        conceptsPanel.removeAllConcepts();

        search("shouldBeNoFieldsForThisCrazySearch");
        verifyThat("Message appearing when no sunburst & search from Sunburst", results.message(), displayed());
        findPage.goToListView();

        conceptsPanel.removeAllConcepts();

        findService.search("shouldAlsoBeNoTopicsForThis");
        findPage.goToSunburst();
        verifyThat("Message appearing when no sunburst & search from elsewhere", results.message(), displayed());
    }

    @Test
    @ResolvedBug("FIND-405")
    public void testParametricSelectors() {
        search("wild horses");

        final int index = filters().nonZeroParamFieldContainer(0);
        final String firstParametric = filters().parametricField(index).filterCategoryName();
        verifyThat("Default parametric selection is 1st parametric type", firstParametric, startsWith(results.getSelectedFieldName(1).toUpperCase()));

        results.parametricSelectionDropdown(2).open();
        verifyThat("1st selected parametric does not appear as choice in 2nd", results.getParametricDropdownItems(2), not(contains(firstParametric)));
    }

    @Test
    public void testParametricSelectorsChangeDisplay() {
        search("cricket");

        final String filterCategory = filters().formattedNameOfNonZeroField(1);
        results.parametricSelectionDropdown(1).select(filterCategory);
        Waits.loadOrFadeWait();

        final int correctNumberSegments = getFilterResultsBigEnoughToDisplay(filterCategory).size();
        assertThat("Correct number (" + correctNumberSegments + ") of sunburst segments ", results.numberOfSunburstSegments(), is(correctNumberSegments));
    }

    @Test
    public void testHoveringOverSegmentCausesTextToChange() {
        search("elephant");

        final String filterCategory = filters().formattedNameOfNonZeroField(0);
        final List<String> bigEnough = getFilterResultsBigEnoughToDisplay(filterCategory);

        for (final WebElement segment : results.findSunburstSegments()) {
            results.segmentHover(segment);
            final String name = results.getSunburstCentreName();
            verifyThat(name, not(isEmptyOrNullString()));
            verifyThat(name, isIn(bigEnough));
        }
    }

    private List<String> getFilterResultsBigEnoughToDisplay(final String filterCategory){
        ParametricFieldContainer filterContainer = filters().parametricContainer(filterCategory);
        filterContainer.expand();
        filterContainer.seeAll();

        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        final List<String> bigEnough = filterModal.expectedParametricValues();
        filterModal.cancel();

        findPage.waitUntilParametricModalGone();
        return bigEnough;
    }

    @Test
    public void testHoveringOnGreySegmentGivesMessage() {
        search("elephant");

        assumeThat("Some segments not displayable", results.greySunburstAreaExists());
        results.hoverOverTooFewToDisplaySegment();

        verifyThat("Hovering on greyed segment explains why grey", results.getSunburstCentreName(), allOf(containsString("Please refine your search"), containsString("too small to display")));
    }

    @Test
    public void testClickingSunburstSegmentFiltersTheSearch() {
        //needs to search something that only has 2 parametric filter types
        search("churchill");

        final String fieldValue = results.hoverOnSegmentGetCentre(1);
        final String fieldName = results.getSelectedFieldName(1);
        LOGGER.info("Filtering by " + fieldName + " = " + fieldValue);
        results.getIthSunburstSegment(1).click();
        results.waitForSunburst();

        verifyThat(findPage.filterLabelsText(), hasItem(containsString(fieldValue)));

        filters().parametricContainer(fieldName).expand();
        verifyThat(filters().checkboxForParametricValue(fieldName, fieldValue), checked());
        verifyThat("Parametric selection name has changed to another type of filter", results.getSelectedFieldName(1), not(fieldName));
    }

    @Test
    @ResolvedBug("FIND-379")
    public void testSideBarFiltersChangeSunburst() {
        search("lashing");

        final FilterPanel filters = filters();
        final String parametricSelectionFirst = results.getSelectedFieldName(1);

        final ParametricFieldContainer container = filters.parametricContainer(parametricSelectionFirst);
        container.expand();
        container.getFilters().get(0).check();

        results.waitForSunburst();
        assertThat("Parametric selection changed", results.getSelectedFieldName(1), not(is(parametricSelectionFirst)));
    }

    //will probably fail if your databases are different to the testing ones
    @Test
    public void testTwoParametricSelectorSunburst() {
        search("cameron");

        results.parametricSelectionDropdown(1).select("Category");
        results.waitForSunburst();
        final int segNumberBefore = results.numberOfSunburstSegments();

        results.parametricSelectionDropdown(2).select("Source");
        results.waitForSunburst();
        final int segNumberAfter = results.numberOfSunburstSegments();

        assertThat("More segments with second parametric selector", segNumberAfter, greaterThan(segNumberBefore));
    }

    //v data dependent -> needs these categories to be mutually exclusive
    @Test
    @ActiveBug("FIND-267")
    public void testNoOverlapParametricFields() {
        search("*");
        results.parametricSelectionDropdown(1).select("Person Sex");
        results.waitForSunburst();
        final int segNumberBefore = results.numberOfSunburstSegments();

        results.parametricSelectionDropdown(2).select("Category");
        results.waitForSunburst();
        final int segNumberAfter = results.numberOfSunburstSegments();

        verifyThat("Same number of segments after 2nd selector", segNumberAfter, is(segNumberBefore));
        verifyThat("Message displayed", results.message(), displayed());
        final String sensibleMessage = "no documents with values for both fields";
        verifyThat("Message contains \"" + sensibleMessage + "\"", results.message().getText(), containsString(sensibleMessage));
    }

    private void search(final String searchTerm) {
        findService.search(searchTerm);
        findPage.goToSunburst();
        results.waitForSunburst();
    }

    private IdolFilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
