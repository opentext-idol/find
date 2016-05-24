package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.shared.QueryTestHelper;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTagName;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class FindITCase extends FindTestBase {
    private FindPage findPage;
    private FindTopNavBar navBar;
    private FindResultsPage results;
    private FindService findService;

    public FindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        navBar = getElementFactory().getTopNavBar();
        results = findPage.getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    public void testSendKeys() throws InterruptedException {
        String searchTerm = "Fred is a chimpanzee";
        findService.search(searchTerm);
        assertThat(navBar.getSearchBoxTerm(), is(searchTerm));
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testSearch() {
        findService.search("Red");
        assertThat(results.getText().toLowerCase(), not(containsString("error")));
    }

    @Test
    public void testBooleanOperators() {
        String termOne = "musketeers";
        String termTwo = "\"dearly departed\"";

        findService.search(termOne);
        List<String> musketeersSearchResults = results.getResultTitles();
        int numberOfMusketeersResults = musketeersSearchResults.size();

        findService.search(termTwo);
        List<String> dearlyDepartedSearchResults = results.getResultTitles();
        int numberOfDearlyDepartedResults = dearlyDepartedSearchResults.size();

        findService.search(termOne + " AND " + termTwo);
        List<String> andResults = results.getResultTitles();
        int numberOfAndResults = andResults.size();

        assertThat(numberOfMusketeersResults, greaterThanOrEqualTo(numberOfAndResults));
        assertThat(numberOfDearlyDepartedResults, greaterThanOrEqualTo(numberOfAndResults));
        String[] andResultsArray = andResults.toArray(new String[andResults.size()]);
        assertThat(musketeersSearchResults, hasItems(andResultsArray));
        assertThat(dearlyDepartedSearchResults, hasItems(andResultsArray));

        findService.search(termOne + " OR " + termTwo);
        List<String> orResults = results.getResultTitles();
        Set<String> concatenatedResults = new HashSet<>(ListUtils.union(musketeersSearchResults, dearlyDepartedSearchResults));
        assertThat(orResults.size(), is(concatenatedResults.size()));
        assertThat(orResults, containsInAnyOrder(concatenatedResults.toArray()));

        findService.search(termOne + " XOR " + termTwo);
        List<String> xorResults = results.getResultTitles();
        concatenatedResults.removeAll(andResults);
        assertThat(xorResults.size(), is(concatenatedResults.size()));
        assertThat(xorResults, containsInAnyOrder(concatenatedResults.toArray()));

        findService.search(termOne + " NOT " + termTwo);
        List<String> notTermTwo = results.getResultTitles();
        Set<String> t1NotT2 = new HashSet<>(concatenatedResults);
        t1NotT2.removeAll(dearlyDepartedSearchResults);
        assertThat(notTermTwo.size(), is(t1NotT2.size()));
        assertThat(notTermTwo, containsInAnyOrder(t1NotT2.toArray()));

        findService.search(termTwo + " NOT " + termOne);
        List<String> notTermOne = results.getResultTitles();
        Set<String> t2NotT1 = new HashSet<>(concatenatedResults);
        t2NotT1.removeAll(musketeersSearchResults);
        assertThat(notTermOne.size(), is(t2NotT1.size()));
        assertThat(notTermOne, containsInAnyOrder(t2NotT1.toArray()));
    }

    //Following 3 correctly failing On_Prem
    @Test
    public void testCorrectErrorMessageDisplayed() {
        new QueryTestHelper<>(findService).booleanOperatorQueryText(Errors.Search.OPERATORS);
        new QueryTestHelper<>(findService).emptyQueryText(Errors.Search.STOPWORDS);
    }

    @Test
    public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
        new QueryTestHelper<>(findService).hiddenQueryOperatorText();
    }

    @Test
    public void testSearchParentheses() {
        new QueryTestHelper<>(findService).mismatchedBracketQueryText();
    }

    @Test
    @ActiveBug({"HOD-2170", "CCUK-3634"})
    public void testSearchQuotationMarks() {
        new QueryTestHelper<>(findService).mismatchedQuoteQueryText(Errors.Search.QUOTES);
    }

    @Test
    @ActiveBug("CCUK-3700")
    public void testWhitespaceSearch() {
        try {
            findService.search("       ");
        } catch (TimeoutException e) { /* Expected behaviour */ }

        assertThat(findPage.footerLogo(), displayed());

        findService.search("Kevin Costner");

        List<String> resultTitles = results.getResultTitles();

        findService.search(" ");
        assertThat(results.getResultTitles(), is(resultTitles));
        verifyThat("Empty Parametric Table does not exist", !findPage.parametricEmptyExists());
        assumeThat(getConfig().getType(), is(ApplicationType.HOSTED));
        assertThat(findPage.parametricContainer().getText(), not(isEmptyOrNullString()));
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

    @Test
    @ResolvedBug("CCUK-3624")
    public void testRefreshEmptyQuery() throws InterruptedException {
        findService.search("something");
        findService.search("");
        Thread.sleep(5000);

        getWindow().refresh();
        findPage = getElementFactory().getFindPage();
        navBar = getElementFactory().getTopNavBar();

        verifyThat(navBar.getSearchBoxTerm(), is(""));
        verifyThat("taken back to landing page after refresh", findPage.footerLogo(), displayed());
    }

}
