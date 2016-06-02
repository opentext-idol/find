package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.filters.FilterNode;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeTrue;


//WARNING: These tests are extremely slow when the sidebar has 1000s of filters

public class IdolFilterITCase extends IdolFindTestBase {
    private FindService findService;

    public IdolFilterITCase(TestConfig config) {
        super(config);}

    @Before
    public void setUp(){
        findService = getApplication().findService();
    }


    //Filters
    @Test
    @ResolvedBug("FIND-122")
    public void testSearchForParametricFieldName(){
        findService.search("face");

        final FilterNode goodField = filters().parametricField(2);
        final String badFieldName = filters().parametricField(0).getParentName();
        final String goodFieldName = goodField.getParentName();
        final String goodFieldValue = goodField.getChildNames().get(0);

        filters().filterResults(goodFieldName);

        assertThat(filters().parametricField(0).getParentName(), not(badFieldName));
        assertThat(filters().parametricField(0).getParentName(), is(goodFieldName));
        assertThat(filters().parametricField(0).getChildNames().get(0), is(goodFieldValue));
    }

    @Test
    public void testSearchForParametricFieldValue(){
        findService.search("face");

        final FilterNode goodField = filters().parametricField(0);
        final String goodFieldName = goodField.getParentName();
        final String badFieldValue = goodField.getChildNames().get(0);
        final String goodFieldValue = goodField.getChildNames().get(1);

        filters().filterResults(goodFieldValue);

        assertThat(filters().parametricField(0).getParentName(), is(goodFieldName));
        assertThat(filters().parametricField(0).getChildNames().get(0), not(badFieldValue));
        assertThat(filters().parametricField(0).getChildNames().get(0), is(goodFieldValue));
    }


    @Test
    public void testSearchForNonExistentFilter() {
        findService.search("face");

        filters().filterResults("garbageasfsefeff");
        assertThat(filters().getErrorMessage(), is("No filters matched"));

        filters().clearFilter();
        assertThat(filters().getErrorMessage(), isEmptyOrNullString());
    }

    //Expanding & Collapsing
    @Test
    public void testExpandFilters(){
        findService.search("face");
        String filter = "IRELAND";

        assumeTrue("Filter IRELAND exists", filters().parametricFilterExists(filter));

        assertThat("Filter not visible",!filters().filterVisible(filter));
        filters().expandFiltersFully();
        assertThat("Filter visible",filters().filterVisible(filter));
        filters().collapseAll();
        assertThat("Filter not visible",!filters().filterVisible(filter));

    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }

}
