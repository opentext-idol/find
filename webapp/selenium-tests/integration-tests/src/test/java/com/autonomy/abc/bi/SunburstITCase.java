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
import com.autonomy.abc.selenium.find.filters.ParametricFilterModal;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
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

    @Before
    public void setUp() {
        findPage = ((BIIdolFindElementFactory)getElementFactory()).getFindPage();
        findService = getApplication().findService();
        conceptsPanel = getElementFactory().getConceptsPanel();
    }

    @Test
    @ResolvedBug("FIND-382")
    public void testMessageOnFirstSearchIfNoValues() {
        results = search("shambolisjlfijslifjeslj");
        results.waitForSunburst();
        verifyThat("Message appearing when no sunburst & search from Sunburst", results.message(), displayed());
    }

    @Test
    @ResolvedBug({"FIND-251", "FIND-382"})
    public void testSunburstTabShowsSunburstOrMessage() {
        results = search("s");

        results.waitForSunburst();

        verifyThat("Sunburst element displayed", results.sunburstVisible());
        verifyThat("Parametric selectors appear", results.parametricSelectionDropdownsExist());

        conceptsPanel.removeAllConcepts();

        results = search("shouldBeNoFieldsForThisCrazySearch");
        verifyThat("Message appearing when no sunburst & search from Sunburst", results.message(), displayed());
        findPage.goToListView();

        conceptsPanel.removeAllConcepts();

        findService.searchAnyView("shouldAlsoBeNoTopicsForThis");
        findPage.goToSunburst();
        verifyThat("Message appearing when no sunburst & search from elsewhere", results.message(), displayed());
    }

    @Test
    @ResolvedBug("FIND-405")
    public void testParametricSelectors() {
        results = search("wild horses");

        final int index = filters().nonZeroParamFieldContainer(0);
        final String firstParametric = filters().parametricField(index).filterCategoryName();
        verifyThat("Default parametric selection is 1st parametric type", firstParametric, startsWith(results.getFirstSelectedFieldName().toUpperCase()));

        results.secondParametricSelectionDropdown().open();
        verifyThat("1st selected parametric does not appear as choice in 2nd", results.getParametricDropdownItems(results.secondParametricSelectionDropdown()), not(contains(firstParametric)));
    }

    @Test
    public void testParametricSelectorsChangeDisplay() {
        results = search("cricket");

        final String filterCategory = filters().formattedNameOfNonZeroField(1);
        results.firstParametricSelectionDropdown().select(filterCategory);
        Waits.loadOrFadeWait();

        final int correctNumberSegments = getFilterResultsBigEnoughToDisplay(filterCategory).size();
        final int actualNumberOfSegments = results.numberOfSunburstSegments();
        assertThat("Correct number (" + correctNumberSegments + ") of sunburst segments ", actualNumberOfSegments, is(correctNumberSegments));
    }

    @Test
    public void testHoveringOverSegmentCausesTextToChange() {
        results = search("b");
        getElementFactory().getFindPage().waitForParametricValuesToLoad();
        //If this test is taking too long, try changing this to avoid filter category w/ 1000+ filters
        final int indexOfFilterCategory = 1;

        final String filterCategory = filters().formattedNameOfNonZeroField(indexOfFilterCategory);
        final List<String> bigEnough = getFilterResultsBigEnoughToDisplay(filterCategory);
        results.firstParametricSelectionDropdown().select(filterCategory);
        results.waitForSunburst();
        Waits.loadOrFadeWait();

        for (final WebElement segment : results.findSunburstSegments()) {
            results.segmentHover(segment);

            final String name = results.getSunburstCentreName();
            verifyThat(name + " is visible/present", name, not(isEmptyOrNullString()));
            verifyThat(name, isIn(bigEnough));
        }
    }

    private List<String> getFilterResultsBigEnoughToDisplay(final String filterCategory){
        filters().parametricContainer(filterCategory).seeAll();

        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        final List<String> bigEnough = filterModal.expectedParametricValues();
        filterModal.cancel();

        findPage.waitUntilParametricModalGone();
        return bigEnough;
    }

    @Test
    public void testHoveringOnGreySegmentGivesMessage() {
        results = search("elephant");

        assumeThat("Some segments not displayable", results.greySunburstAreaExists());

        results.hoverOverTooFewToDisplaySegment();
        LOGGER.info("Test can be brittle due to difficulty of interacting with sunburst (SVG).");
        verifyThat("Hovering on greyed segment explains why grey", results.getSunburstCentreName(), allOf(containsString("Please refine your search"), containsString("too small to display")));
    }

    @Test
    //needs to search something that only has 2 parametric filter types
    public void testClickingSunburstSegmentFiltersTheSearch() {
        results = search("general");

        LOGGER.info("Test only works if filtering by the clicked filter leaves other filters in different categories clickable");
        results.firstParametricSelectionDropdown().selectItem(1);
        results.waitForSunburst();

        final String fieldValue = results.hoverOnSegmentGetCentre(1);
        final String fieldName = results.getFirstSelectedFieldName();
        LOGGER.info("Filtering by " + fieldName + " = " + fieldValue);
        results.getIthSunburstSegment(1).click();
        results.waitForSunburst();

        verifyThat(findPage.filterLabelsText(), hasItem(containsString(fieldValue)));

        verifyThat(filters().checkboxForParametricValue(fieldName, fieldValue), checked());
        verifyThat("Parametric selection name has changed to another type of filter", results.getFirstSelectedFieldName(), not(fieldName));
    }

    @Test
    @ResolvedBug("FIND-379")
    public void testSideBarFiltersChangeSunburst() {
        results = search("lashing");

        final FilterPanel filters = filters();
        final String parametricSelectionFirst = results.getFirstSelectedFieldName();

        filters.parametricContainer(parametricSelectionFirst).getFilters().get(0).check();

        results.waitForSunburst();
        assertThat("Parametric selection changed", results.getFirstSelectedFieldName(), not(is(parametricSelectionFirst)));
    }

    //will probably fail if your databases are different to the testing ones
    @Test
    public void testTwoParametricSelectorSunburst() {
        results = search("cameron");

        results.firstParametricSelectionDropdown().select("Overall Vibe");
        results.waitForSunburst();
        final int segNumberBefore = results.numberOfSunburstSegments();

        results.secondParametricSelectionDropdown().select("Source");
        results.waitForSunburst();
        final int segNumberAfter = results.numberOfSunburstSegments();

        assertThat("More segments with second parametric selector", segNumberAfter, greaterThan(segNumberBefore));
    }

    @Test
    public void testTwoParametricSelectorSwapButton() {
        results = search("pony");

        final String firstField = "Source";

        results.firstParametricSelectionDropdown().select(firstField);
        results.waitForSunburst();
        final String secondField = "Overall Vibe";
        results.secondParametricSelectionDropdown().select(secondField);
        results.waitForSunburst();

        results.clickSwapButton();
        results.waitForSunburst();
        final String newFirstField = results.getFirstSelectedFieldName();
        final String newSecondField = results.getSecondSelectedFieldName();

        assertThat("Fields have not swapped", firstField.equals(newSecondField) && secondField.equals(newFirstField));
    }

    //v data dependent -> needs these categories to be mutually exclusive
    @Test
    @ResolvedBug("FIND-267")
    public void testNoOverlapParametricFields() {
        results = search("*");
        results.firstParametricSelectionDropdown().select("Category");
        results.waitForSunburst();
        final int segNumberBefore = results.numberOfSunburstSegments();

        results.secondParametricSelectionDropdown().select("Place");
        results.waitForSunburst();
        final int segNumberAfter = results.numberOfSunburstSegments();

        verifyThat("Same number of segments after 2nd selector", segNumberAfter, is(segNumberBefore));
        verifyThat("Message displayed", results.message(), displayed());
        final String sensibleMessage = "no documents with values for both fields";
        verifyThat("Message contains \"" + sensibleMessage + '"', results.message().getText(), containsString(sensibleMessage));
    }

    private SunburstView search(final String searchTerm) {
        findService.searchAnyView(searchTerm);
        results = findPage.goToSunburst();
        results.waitForSunburst();
        return results;
    }

    private IdolFilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
