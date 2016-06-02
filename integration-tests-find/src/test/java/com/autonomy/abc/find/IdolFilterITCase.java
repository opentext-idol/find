package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeTrue;


//WARNING: These tests are extremely slow when the sidebar has 1000s of filters

public class IdolFilterITCase extends IdolFindTestBase {
    private IdolFindPage findPage;
    private FindService findService;

    public IdolFilterITCase(TestConfig config) {
        super(config);}

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }


    //Filters
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
    @ActiveBug("FIND-122")
    public void testSearchFilters(){
        findService.search("face");

        //search for filter that isn't present
        findPage.filterResults("asfsefeff");
        assertThat("No filter matched", findPage.noneMatchingMessageVisible());

        findPage.clearFilter();

        //if not 1000s of filters -> uncomment below
        //checkCorrectFiltersVisible(Arrays.asList("UNITED STATES OF AMERICA","Last Week","Test","PDF"));
        //if 1000s of filters -> test what was broken
        checkCorrectFiltersVisible(Collections.singletonList("Last Week"));
    }

    private void checkCorrectFiltersVisible(List<String> filtersToSearch){

        for (String targetFilter:filtersToSearch){

            findPage.clearFilter();
            findPage.expandFiltersFully();

            List<WebElement> allFilters = findPage.getCurrentFilters();
            List<String> matchingFilters = findPage.findFilterString(targetFilter,allFilters);

            findPage.filterResults(targetFilter);

            findPage.showFilters();

            List<String> visibleFilters = ElementUtil.getTexts(findPage.getCurrentFilters());

            assertThat("Filtering with "+targetFilter+" shows the right number filters "+visibleFilters.size(),visibleFilters.size(),is(matchingFilters.size()));


            Collections.sort(visibleFilters);
            Collections.sort(matchingFilters);

            assertThat("All filters that should be displayed are", matchingFilters.equals(visibleFilters));
        }

    }

    //Expanding & Collapsing
    @Test
    public void testExpandFilters(){
        findService.search("face");
        String filter = "IRELAND";

        assumeTrue("Filter IRELAND exists",findPage.parametricFilterExists(filter));

        assertThat("Filter not visible",!findPage.filterVisible(filter));
        findPage.expandFiltersFully();
        assertThat("Filter visible",findPage.filterVisible(filter));
        findPage.collapseAll();
        assertThat("Filter not visible",!findPage.filterVisible(filter));

    }

}
