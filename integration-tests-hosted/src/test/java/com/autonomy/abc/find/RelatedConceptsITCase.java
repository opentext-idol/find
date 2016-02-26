package com.autonomy.abc.find;

import com.autonomy.abc.config.FindTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;

public class RelatedConceptsITCase extends FindTestBase {
    private FindService findService;
    private FindResultsPage results;

    public RelatedConceptsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        results = getElementFactory().getResultsPage();
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
        FindTopNavBar navBar = getElementFactory().getTopNavBar();
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

    // TODO: testMultiWordSearchTermInResults
    @Test
    public void testRelatedConceptsInResults(){
        findService.search("Tiger");

        for(WebElement relatedConceptLink : results.relatedConcepts()){
            String relatedConcept = relatedConceptLink.getText();
            for (WebElement relatedConceptElement : getDriver().findElements(By.xpath("//*[contains(@class,'middle-container')]//*[not(self::h4) and contains(text(),'" + relatedConcept + "')]"))) {
                if (relatedConceptElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                    verifyThat(relatedConceptElement, containsTextIgnoringCase(relatedConcept));
                }
                verifyThat(relatedConceptElement, hasTagName("a"));
                verifyThat(relatedConceptElement, hasClass("clickable"));
            }
        }
    }
}
