package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.results.RelatedConceptsPanel;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsTextIgnoringCase;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTextThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

@RelatedTo("CSA-2091")
@Role(UserRole.FIND)
public class RelatedConceptsITCase extends FindTestBase {
    private FindService findService;
    private ConceptsPanel conceptsPanel;
    private FindTopNavBar navBar;

    public RelatedConceptsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        final FindElementFactory elementFactory = getElementFactory();
        findService = getApplication().findService();

        findService.search("Danye West");

        conceptsPanel = elementFactory.getConceptsPanel();
        navBar = elementFactory.getTopNavBar();
    }

    @Test
    public void testRelatedConceptsHasResults() {
        for (final WebElement concept : relatedConceptsPanel()) {
            assertThat(concept, hasTextThat(not(isEmptyOrNullString())));
            assertThat(concept, not(containsTextIgnoringCase("loading")));
        }
    }

    @Test
    @RelatedTo("CCUK-3598")
    public void testRelatedConceptsNavigateOnClick() {
        final String search = "Red";
        findService.search(search);

        final WebElement topRelatedConcept = relatedConceptsPanel().concept(0);
        final String concept = topRelatedConcept.getText();

        topRelatedConcept.click();
        assertThat(conceptsPanel.selectedConceptHeaders(), hasItem(equalToIgnoringCase(concept)));
        assertThat(navBar.getSearchBoxTerm(), is(search));
    }

    @Test
    @ResolvedBug({"CCUK-3498", "CSA-2066"})
    public void testRelatedConceptsHover() {
        final String popover = relatedConceptsPanel().hoverOverRelatedConcept(0).getText();
        verifyThat(popover, not(isEmptyOrNullString()));
        verifyThat(popover, not(containsString("QueryText-Placeholder")));
        verifyThat(popover, not(containsString(Errors.Search.RELATED_CONCEPTS)));
    }

    @Test
    public void testMultipleAdditionalConcepts() {
        findService.search("bongo");

        final Collection<String> relatedConcepts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final List<WebElement> newRelatedConcepts = relatedConceptsPanel().relatedConcepts();

            if (!newRelatedConcepts.isEmpty()) {
                final String newConcept = clickFirstNewConcept(relatedConcepts, newRelatedConcepts);
                verifyThat(conceptsPanel.selectedConceptHeaders(), hasItem(equalToIgnoringCase(newConcept)));
            }
        }

        verifyThat(navBar.getSearchBoxTerm(), is("bongo"));

        final List<String> selectedConceptHeaders = conceptsPanel.selectedConceptHeaders();
        verifyThat(selectedConceptHeaders, hasSize(relatedConcepts.size()));
        verifyThat(selectedConceptHeaders, containsItems(relatedConcepts));
    }

    @Test
    @Category(CoreFeature.class)
    public void testAddRemoveConcepts() {
        findService.search("jungle");
        final Collection<String> concepts = new ArrayList<>();
        final String firstConcept = clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());
        final String secondConcept = clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());

        verifyThat(conceptsPanel.selectedConceptElements(), hasSize(2));

        conceptsPanel.removableConceptForHeader(secondConcept).removeAndWait();

        final List<String> moreConcepts = conceptsPanel.selectedConceptHeaders();

        verifyThat(moreConcepts, hasSize(1));
        verifyThat(moreConcepts, not(hasItem(equalToIgnoringCase(secondConcept))));
        verifyThat(moreConcepts, hasItem(equalToIgnoringCase(firstConcept)));
        verifyThat(navBar.getSearchBoxTerm(), is("jungle"));
    }

    @Test
    @ResolvedBug({"CCUK-3566", "FIND-109"})
    @ActiveBug("FIND-495")
    public void testTermNotInRelatedConcepts() {
        final String query = "world cup";
        findService.search(query);
        final RelatedConceptsPanel panel = relatedConceptsPanel();

        verifyThat(panel.getRelatedConcepts(), not(hasItem(equalToIgnoringCase(query))));

        final Collection<String> addedConcepts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            clickFirstNewConcept(addedConcepts, relatedConceptsPanel().relatedConcepts());
            verifyThat(panel.getRelatedConcepts(), not(hasItem(equalToIgnoringCase(query))));
        }
    }

    @Test
    @ResolvedBug("CCUK-3566")
    public void testAdditionalConceptsNotAlsoRelated() {
        findService.search("matt");
        final Collection<String> addedConcepts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            clickFirstNewConcept(addedConcepts, relatedConceptsPanel().relatedConcepts());
            final List<String> relatedConcepts = relatedConceptsPanel().getRelatedConcepts();

            for (final String addedConcept : addedConcepts) {
                verifyThat(relatedConcepts, not(hasItem(equalToIgnoringCase(addedConcept))));
            }
        }
    }

    @Test
    @RelatedTo({"FIND-243", "FIND-110"})
    public void testRefreshAddedConcepts() {
        findService.search("fresh");
        final Collection<String> concepts = new ArrayList<>();
        clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());
        clickFirstNewConcept(concepts, relatedConceptsPanel().relatedConcepts());

        getWindow().refresh();
        navBar = getElementFactory().getTopNavBar();

        verifyThat(navBar.getSearchBoxTerm(), is("fresh"));
        LOGGER.info("Test will always currently fail due to lack of routing/push-state");
        verifyThat(conceptsPanel.selectedConceptHeaders(), containsItems(concepts));
    }

    @Test
    @ActiveBug("FIND-308")
    public void testRelatedConceptsHoverNoExtraScrollBar() {
        findService.search("orange");
        //if few related concepts then bug not happen
        if (relatedConceptsPanel().relatedConceptsClusters().size() >= 2) {
            final List<WebElement> clusterMembers = relatedConceptsPanel().membersOfCluster(1);
            final int lastConcept = clusterMembers.size() - 1;
            relatedConceptsPanel().hoverOverRelatedConcept(clusterMembers.get(lastConcept));
            verifyThat("No vertical scroll bar", !getElementFactory().getFindPage().verticalScrollBarPresent());
        } else {
            LOGGER.warn("There were too few concept clusters to carry out this test - bug would not occur");
        }
    }

    private String clickFirstNewConcept(final Collection<String> existingConcepts, final Iterable<WebElement> relatedConcepts) {
        for (final WebElement concept : relatedConcepts) {
            final String conceptText = concept.getText();
            if (!existingConcepts.contains(conceptText)) {
                LOGGER.info("Clicking concept " + conceptText);
                concept.click();

                existingConcepts.add(conceptText.toLowerCase());

                return conceptText;
            }
        }

        throw new NoSuchElementException("no new related concepts");
    }

    private RelatedConceptsPanel relatedConceptsPanel() {
        return getElementFactory().getRelatedConceptsPanel();
    }
}
