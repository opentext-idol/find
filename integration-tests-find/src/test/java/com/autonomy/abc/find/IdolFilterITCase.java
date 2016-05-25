package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeTrue;

public class IdolFilterITCase extends FindTestBase {
    private IdolFindPage findPage;
    private FindTopNavBar navBar;
    private FindResultsPage results;
    private FindService findService;

    public IdolFilterITCase(TestConfig config) {
        super(config);}

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.ON_PREM));
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getIdolFindPage();
        navBar = getElementFactory().getTopNavBar();
        results = findPage.getResultsPage();
        findService = getApplication().findService();
    }


    //Filters - following are crazy long if there are 1000s of filters
    @Test
    @ActiveBug("FIND-122")
    public void testSearchFilterTypes(){
        findService.search("face");
        findPage.expandFiltersFully();

        //if there aren't several 1000 filters -> run through all the filter types: uncomment following line
        //checkCorrectFiltersVisible(findPage.getVisibleFilterTypes(),allFilters);
        checkCorrectFiltersVisible(Arrays.asList("DATABASES","DATES","CATEGORY"));
    }

    @Test
    public void testSearchFilters(){
        findService.search("face");

        //search for filter that isn't present
        findPage.filterResults("asfsefeff");
        assertThat("No filter matched", findPage.noneMatchingMessageVisible());

        findPage.clearFilter();

        //if not 1000s of filters -> uncomment below
        //checkCorrectFiltersVisible(Arrays.asList("UNITED STATES OF AMERICA","Last Week","Test","PDF"));
        //if 1000s of filters -> test what was broken
        checkCorrectFiltersVisible(Arrays.asList("Last Week"));

    }

    private void checkCorrectFiltersVisible(List<String> filtersToSearch){

        for (String targetFilter:filtersToSearch){

            findPage.clearFilter();
            findPage.expandFiltersFully();

            List<WebElement> allFilters = findPage.getCurrentFiltersIncType();
            List<String> matchingFilters = findPage.findFilterString(targetFilter,allFilters);

            findPage.filterResults(targetFilter);
            findPage.showFilters();

            List<String> visibleFilters = ElementUtil.getTexts(findPage.getCurrentFiltersIncType());

            verifyThat("Filtering with "+targetFilter+" shows the right number filters "+visibleFilters.size(),visibleFilters.size(),is(matchingFilters.size()));

            Collections.sort(visibleFilters);
            Collections.sort(matchingFilters);
            verifyThat("All filters that should be displayed are", matchingFilters.equals(visibleFilters));
        }

    }

    //Expanding & Collapsing
    @Test
    public void testExpandFilters(){
        findService.search("face");
        String filter = "IRELAND";
        assumeTrue("Filter IRELAND exists",findPage.filterExists(filter));

        assertThat("Filter not visible",!findPage.filterVisible(filter));
        findPage.expandFiltersFully();
        assertThat("Filter visible",findPage.filterVisible(filter));
        findPage.collapseAll();
        assertThat("Filter not visible",!findPage.filterVisible(filter));

    }

}
