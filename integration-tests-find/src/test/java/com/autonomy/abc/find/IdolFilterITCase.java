package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.filters.ListFilterContainer;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class IdolFilterITCase extends IdolFindTestBase {
    private FindService findService;

    public IdolFilterITCase(final TestConfig config) {
        super(config);}

    @Before
    public void setUp(){
        findService = getApplication().findService();
        getElementFactory().getFindPage().goToListView();
    }

    //Filters
    @Test
    @ResolvedBug("FIND-122")
    public void testSearchForParametricFieldName(){
        findService.search("face");

        final ListFilterContainer goodField = filters().parametricField(2);
        final String badFieldName = filters().parametricField(0).getParentName();
        final String goodFieldName = goodField.getParentName();
        final String goodFieldValue = goodField.getFilterNames().get(0);

        filters().collapseAll();

        filters().filterResults(goodFieldName);

        assertThat(filters().parametricField(0).getParentName(), not(badFieldName));
        assertThat(filters().parametricField(0).getParentName(), is(goodFieldName));
        assertThat(filters().parametricField(0).getFilterNames().get(0), is(goodFieldValue));
    }

    @Test
    public void testSearchForParametricFieldValue(){
        findService.search("face");

        final ListFilterContainer goodField = filters().parametricField(0);
        final String goodFieldName = goodField.getParentName();
        final String badFieldValue = goodField.getFilterNames().get(0);
        final String goodFieldValue = goodField.getFilterNames().get(1);

        filters().filterResults(goodFieldValue);

        assertThat(filters().parametricField(0).getParentName(), is(goodFieldName));
        assertThat(filters().parametricField(0).getFilterNames().get(0), not(badFieldValue));
        assertThat(filters().parametricField(0).getFilterNames().get(0), is(goodFieldValue));
    }

    @Test
    public void testSearchForNonExistentFilter() {
        findService.search("face");

        filters().filterResults("garbageasfsefeff");
        assertThat(filters().getErrorMessage(), is("No filters matched"));

        filters().clearFilter();
        assertThat(filters().getErrorMessage(), isEmptyOrNullString());
    }

    @Test
    public void testNumericWidgetsDefaultCollapsed() {
        findService.search("swim");

        for(GraphFilterContainer container : filters().graphContainers()) {
            verifyThat("Widget is collapsed",container.isCollapsed());
        }
    }

    private IdolFilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }

}
