package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.save.SavedSearchPanel;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchTab;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class IdolAutoCorrectITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;
    public IdolAutoCorrectITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        findPage = getElementFactory().getFindPage();
    }

    @After
    public void tearDown() {
        if (hasSetUp()) {
            findService.search("back to results");
            getApplication().savedSearchService().deleteAll();
        }
    }

    @Test
    @ActiveBug("FIND-357")
    public void testSavingAutoCorrectedSearch() {
        SavedSearchService savedSearchService = getApplication().savedSearchService();
        final String searchName = "SaveMcSave";

        String correctedQuery = saveAndVerify(savedSearchService, searchName, SearchType.SNAPSHOT);

        getElementFactory().getSearchTabBar().switchTo(searchName);

        verifyThat("Correct query text saved", new SavedSearchPanel(getDriver()).queryText(), is(correctedQuery));

        savedSearchService.openNewTab();
        saveAndVerify(savedSearchService, "eejit", SearchType.QUERY);
    }

    private String saveAndVerify(SavedSearchService savedSearchService, String searchName, SearchType type) {
        findService.search("purble");
        findPage.waitForParametricValuesToLoad();
        String correctedQuery = findPage.correctedQuery();

        savedSearchService.saveCurrentAs(searchName, type);

        getElementFactory().getSearchTabBar().switchTo(searchName);

        final SearchTab currentTab = getElementFactory().getSearchTabBar().currentTab();
        assertThat(currentTab.getTitle(), is(searchName));
        assertThat(currentTab.getTitle(), not(containsString("New Search")));

        return correctedQuery;
    }

    // Also applies to:CSV exports
    @Test
    @ActiveBug({"FIND-176","FIND-453"})
    public void testAutoCorrectAffectsAllTheThings() {
        findService.search("jedu");
        findPage.waitForParametricValuesToLoad();

        assertThat("Term auto-corrected",findPage.originalQuery(), displayed());

        IdolFilterPanel filterPanel = getElementFactory().getFilterPanel();
        verifyThat("Parametric fields loaded",!filterPanel.noParametricFields());

        findPage.goToListView();
        assertThat("There are results in list view",findPage.totalResultsNum(),greaterThan(0));

        findPage.goToTopicMap();
        TopicMapView topicMap = getElementFactory().getTopicMap();

        verifyThat("Error message not shown",topicMap.emptyMessage(), not(displayed()));
        verifyThat("Topic map shown",topicMap.topicMapVisible());

        findPage.goToSunburst();
        SunburstView sunburstView = getElementFactory().getSunburst();
        verifyThat("Sunburst shown",sunburstView.sunburstVisible());
        //TODO: verify no error message once know what error message will look like
    }


}
