package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTagName;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;

public class ResultsITCase extends FindTestBase {
    private FindPage findPage;
    private FindResultsPage results;
    private FindService findService;

    public ResultsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        results = findPage.getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    @ResolvedBug("CSA-1665")
    public void testSearchTermInResults() {
        String searchTerm = "tiger";

        findService.search(searchTerm);

        for (WebElement searchElement : getDriver().findElements(By.xpath("//*[contains(@class,'search-text') and contains(text(),'" + searchTerm + "')]"))) {
            if (searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
                verifyThat(searchElement.getText().toLowerCase(), containsString(searchTerm));
            }
            verifyThat(searchElement, not(hasTagName("a")));
        }
    }

    //Both correctly failing last part because some titles are duplicates
    @Test
    @ActiveBug("CSA-2082")
    public void testAutoScroll() {
        findService.search("nightmare");

        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(30), lessThanOrEqualTo(60)));

        findPage.scrollToBottom();
        verifyThat(results.getResults().size(), allOf(greaterThanOrEqualTo(60), lessThanOrEqualTo(90)));

        List<String> titles = results.getResultTitles();
        Set<String> titlesSet = new HashSet<>(titles);

        verifyThat("No duplicate titles", titles.size(), is(titlesSet.size()));
    }

    @Test
    @ResolvedBug("CCUK-3647")
    public void testNoMoreResultsFoundAtEnd() {
        findService.search(new Query("oesophageal"));

        verifyThat(results.getResults().size(), lessThanOrEqualTo(30));

        findPage.scrollToBottom();
        verifyThat(results.resultsDiv(), containsText("No more results found"));
    }

    @Test
    @ActiveBug("FIND-93")
    public void testNoResults() {
        findService.search("thissearchwillalmostcertainlyreturnnoresults");

        verifyThat(results.resultsDiv(), either(containsText("No results found")).or(containsText("No more results found")));

        findPage.scrollToBottom();

        int occurrences = StringUtils.countMatches(results.resultsDiv().getText(), "results found");
        verifyThat("Only one message showing at the bottom of search results", occurrences, is(1));
    }

}
