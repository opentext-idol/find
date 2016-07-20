import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryResultsPage;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.QueryTestHelper;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.stringContainingAnyOf;


public class IdolQueryTestHelper extends QueryTestHelper{
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestHelper.class);

    public IdolQueryTestHelper(final QueryService<ResultsView> queryService){super(queryService);}

    @Override
    public void hiddenQueryOperatorText() {
        for (IdolResult result : resultsFor(getHiddenBooleans())) {
            if (result.errorContainer().isDisplayed()) {
                if(!result.correctedQuery().isDisplayed()){
                    verifyThat("Query not auto-corrected thus error is for no results",result.errorContainer(), containsText(Errors.Search.NO_RESULTS));
                }
                else{
                    verifyThat("Query auto-corrected so sees the Boolean",result.getErrorMessage(),stringContainingAnyOf(Arrays.asList(Errors.Search.CLOSING_BOOL,Errors.Search.OPENING_BOOL)));
                }
            } else {
                LOGGER.info("The error message is not displayed.");
            }
        }

    }


    private class IdolResult extends Result{
        private final ResultsView page;

        IdolResult(final String term, final ResultsView currentPage) {
            super(term,currentPage);
            page = currentPage;
        }

        WebElement errorContainer() {
            return getPage().errorContainer();
        }

        WebElement correctedQuery(){
            return page.correctedQuery();
        }
    }


}
