package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.filters.ListFilterContainer;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class IdolFilterITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;

    public IdolFilterITCase(final TestConfig config) {
        super(config);}

    @Before
    public void setUp(){
        findService = getApplication().findService();
        findPage = getElementFactory().getFindPage();
        findPage.goToListView();
    }

    private ResultsView search(final String searchTerm) {
        final ResultsView results = findService.search(searchTerm);
        findPage.waitForParametricValuesToLoad();
        return results;
    }

    //Filters
    @Test
    @ResolvedBug("FIND-122")
    public void testSearchForParametricFieldName(){
        findService.search("face");

        final ListFilterContainer goodField = filters().parametricField(2);
        final String badFieldName = filters().parametricField(0).filterCategoryName();
        final String goodFieldName = goodField.filterCategoryName();
        final String goodFieldValue = goodField.getFilterNames().get(0);

        filters().collapseAll();

        filters().filterResults(goodFieldName);

        assertThat(filters().parametricField(0).filterCategoryName(), not(badFieldName));
        assertThat(filters().parametricField(0).filterCategoryName(), is(goodFieldName));
        assertThat(filters().parametricField(0).getFilterNames().get(0), is(goodFieldValue));
    }

    @Test
    public void testSearchForParametricFieldValue(){
        findService.search("face");

        final ListFilterContainer goodField = filters().parametricField(0);
        final String goodFieldName = goodField.filterCategoryName();
        final String badFieldValue = goodField.getFilterNames().get(0);
        final String goodFieldValue = goodField.getFilterNames().get(1);

        filters().filterResults(goodFieldValue);

        assertThat(filters().parametricField(0).filterCategoryName(), is(goodFieldName));
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


    @Test
    public void testParametricFiltersOpenWhenMatchingFilter() {
        search("haven");

        final IdolFilterPanel filterPanel = filters();

        // we look up filterPanel.parametricField(0) every time to avoid stale elements (when the filter is changed all the views are destroyed and recreated)
        final String firstValue = filterPanel.parametricField(0).getFilterNames().get(0);

        verifyThat(filterPanel.parametricField(0).isCollapsed(), Matchers.is(true));

        filterPanel.filterResults(firstValue);

        verifyThat(filterPanel.parametricField(0).isCollapsed(), Matchers.is(false));

        filterPanel.clearFilter();

        verifyThat(filterPanel.parametricField(0).isCollapsed(), Matchers.is(true));
    }

    @Test
    public void testParametricFilterRemembersStateWhenMetaFiltering() {
        search("haven");

        final IdolFilterPanel filterPanel = filters();

        // we look up filterPanel.parametricField(0) every time to avoid stale elements (when the filter is changed all the views are destroyed and recreated)
        final String firstValue = filterPanel.parametricField(0).getFilterNames().get(0);

        filterPanel.parametricField(0).expand();

        verifyThat(filterPanel.parametricField(0).isCollapsed(), Matchers.is(false));

        filterPanel.filterResults(firstValue);

        verifyThat(filterPanel.parametricField(0).isCollapsed(), Matchers.is(false));

        filterPanel.clearFilter();

        verifyThat(filterPanel.parametricField(0).isCollapsed(), Matchers.is(false));
    }

    @Test
    public void testIndexesOpenWhenMatchingMetaFilter() {
        search("haven");

        final IdolFilterPanel filterPanel = filters();

        final ListFilterContainer indexesTreeContainer = filterPanel.indexesTreeContainer();
        final IndexesTree indexes = filterPanel.indexesTree();
        final String firstValue = indexes.allIndexes().getIndex(0).getName();

        verifyThat(indexesTreeContainer.isCollapsed(), Matchers.is(false));

        filterPanel.filterResults(firstValue);

        verifyThat(indexesTreeContainer.isCollapsed(), Matchers.is(false));

        filterPanel.clearFilter();

        verifyThat(indexesTreeContainer.isCollapsed(), Matchers.is(false));
    }

    @Test
    public void testIndexesRememberStateWhenMetaFiltering() {
        search("haven");

        final IdolFilterPanel filterPanel = filters();

        final ListFilterContainer indexesTreeContainer = filterPanel.indexesTreeContainer();
        final IndexesTree indexes = filterPanel.indexesTree();
        final String firstValue = indexes.allIndexes().getIndex(0).getName();

        indexesTreeContainer.collapse();

        verifyThat(indexesTreeContainer.isCollapsed(), Matchers.is(true));

        filterPanel.filterResults(firstValue);

        verifyThat(indexesTreeContainer.isCollapsed(), Matchers.is(false));

        filterPanel.clearFilter();

        verifyThat(indexesTreeContainer.isCollapsed(), Matchers.is(true));
    }

    private IdolFilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }

}
