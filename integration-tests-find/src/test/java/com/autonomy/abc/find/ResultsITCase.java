package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTagName;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class ResultsITCase extends FindTestBase {
    private FindPage findPage;
    private FindService findService;

    public ResultsITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        if(!isHosted()) {
            ((IdolFindPage) findPage).goToListView();
        }
    }

    @Test
    @ResolvedBug("CSA-1665")
    public void testSearchTermInResults() {
        final String searchTerm = "tiger";

        findService.search(searchTerm);

        for (final WebElement searchElement : getDriver().findElements(By.xpath("//*[contains(@class,'search-text') and contains(text(),'" + searchTerm + "')]"))) {
            if (searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText().toLowerCase(), containsString(searchTerm));
            }
            verifyThat(searchElement, not(hasTagName("a")));
        }
    }

    @Test
    @ResolvedBug("CSA-2082")
    public void testAutoScroll() {
        ResultsView results = findService.search("nightmare");

        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.getResults(), hasSize(allOf(greaterThanOrEqualTo(30), lessThanOrEqualTo(60))));

        findPage.scrollToBottom();
        verifyThat(results.getResults(), hasSize(allOf(greaterThanOrEqualTo(60), lessThanOrEqualTo(90))));

        List<String> references = new ArrayList<>();

        for(FindResult result : results.getResults()){
            references.add(result.getReference());
        }

        Set<String> referencesSet = new HashSet<>(references);

        /* References apparently may not be unique, but they're definitely ~more unique
                than titles within our data set  */
        verifyThat("No duplicate references", references, hasSize(referencesSet.size()));
    }

    @Test
    @ResolvedBug("CCUK-3647")
    public void testNoMoreResultsFoundAtEnd() {
        ResultsView results = findService.search(new Query("Cheese AND Onion"));

        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.resultsDiv(), containsText("No more results found"));
    }

    @Test
    @ResolvedBug("FIND-93")
    public void testNoResults() {
        ResultsView results = findService.search("thissearchwillalmostcertainlyreturnnoresults");

        verifyThat(results.resultsDiv(), containsText("No results found"));

        findPage.scrollToBottom();

        final int occurrences = StringUtils.countMatches(results.resultsDiv().getText(), "results found");
        verifyThat("Only one message showing at the bottom of search results", occurrences, is(1));
    }

    @Test
    @ActiveBug("FIND-350")
    public void testDecliningAutoCorrectNotPermanent() {
        search("blarf");

        findPage.originalQuery().click();
        findPage.waitForParametricValuesToLoad();

        search("jedu");
        verifyThat("Says it corrected query",findPage.originalQuery(), displayed());

        if(!isHosted()) {
            ((IdolFindPage) findPage).goToListView();
        }
        verifyThat("There are results in list view",findPage.totalResultsNum(),greaterThan(0));
    }


    private void search(String term) {
        findService.search(term);
        findPage.waitForParametricValuesToLoad();
    }

}
