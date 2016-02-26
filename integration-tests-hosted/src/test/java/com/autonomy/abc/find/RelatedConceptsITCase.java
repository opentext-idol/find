package com.autonomy.abc.find;

import com.autonomy.abc.config.FindTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

@RelatedTo("CSA-2091")
public class RelatedConceptsITCase extends FindTestBase {
    private FindService findService;
    private FindResultsPage results;
    private FindTopNavBar navBar;


    public RelatedConceptsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        results = getElementFactory().getResultsPage();
        navBar = getElementFactory().getTopNavBar();
    }

    //TODO ALL RELATED CONCEPTS TESTS - probably better to check if text is not("Loading...") rather than not("")
    @Test
    public void testRelatedConceptsHasResults(){
        findService.search("Danye West");
        for (WebElement concept : results.relatedConcepts()) {
            assertThat(concept, hasTextThat(not(isEmptyOrNullString())));
        }
    }

    @Test
    public void testRelatedConceptsNavigateOnClick(){
        String search = "Red";
        findService.search(search);
        WebElement topRelatedConcept = results.relatedConcepts().get(0);
        String concept = topRelatedConcept.getText();

        topRelatedConcept.click();
        assertThat(navBar.getAlsoSearchingForTerms(), hasItem(concept));
        assertThat(navBar.getSearchBoxTerm(), is(search));
    }

    @Test
    @KnownBug({"CCUK-3498", "CSA-2066"})
    public void testRelatedConceptsHover(){
        findService.search("Find");
        WebElement popover = results.hoverOverRelatedConcept(0);
        verifyThat(popover, hasTextThat(not(isEmptyOrNullString())));
        verifyThat(popover.getText(), not(containsString("QueryText-Placeholder")));
        verifyThat(popover.getText(), not(containsString(Errors.Search.RELATED_CONCEPTS)));
        results.unhover();
    }

    @Test
    public void testRelatedConceptsInResults(){
        findService.search("Tiger");
        results.highlightRelatedConceptsButton().click();

        for(WebElement relatedConceptLink : results.relatedConcepts()) {
            String relatedConcept = relatedConceptLink.getText();
            for (WebElement relatedConceptElement : results.highlightedSausages(relatedConcept)) {
                if (relatedConceptElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                    verifyThat(relatedConceptElement, containsTextIgnoringCase(relatedConcept));
                }
                verifyThat(relatedConceptElement, hasTagName("a"));
                verifyThat(relatedConceptElement, hasClass("clickable"));
            }
        }
    }

    @Test
    @RelatedTo("CCUK-3599")
    public void testRelatedConceptsHighlightButton() {
        findService.search("pancakes");
        WebElement button = results.highlightRelatedConceptsButton();

        verifyThat(results.highlightedSausages(""), empty());
        verifyThat(button, not(hasClass("active")));

        button.click();
        verifyThat(results.highlightedSausages(""), not(empty()));
        verifyThat(button, hasClass("active"));

        button.click();
        verifyThat(results.highlightedSausages(""), empty());
        verifyThat(button, not(hasClass("active")));
    }

    @Test
    public void testMultipleAdditionalConcepts() {
        findService.search("bongo");

        List<String> relatedConcepts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String newConcept = clickFirstNewConcept(relatedConcepts);
            verifyThat(navBar.getAlsoSearchingForTerms(), hasItem(newConcept));
        }
        verifyThat(navBar.getSearchBoxTerm(), is("bongo"));
        verifyThat(navBar.getAlsoSearchingForTerms(), hasSize(relatedConcepts.size()));
        verifyThat(navBar.getAlsoSearchingForTerms(), containsItems(relatedConcepts));
    }

    private String clickFirstNewConcept(List<String> existingConcepts) {
        for (WebElement concept : results.relatedConcepts()) {
            String conceptText = concept.getText();
            if (!existingConcepts.contains(conceptText)) {
                concept.click();
                existingConcepts.add(conceptText);
                results.unhover();
                return conceptText;
            }
        }
        throw new NoSuchElementException("no new related concepts");
    }
}
