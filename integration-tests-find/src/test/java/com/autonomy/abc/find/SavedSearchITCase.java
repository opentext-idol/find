package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchTab;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SavedSearchITCase extends IdolFindTestBase {
    private FindService findService;
    private SavedSearchService saveService;

    public SavedSearchITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findService = getApplication().findService();
        saveService = getApplication().savedSearchService();
    }

    @After
    public void tearDown() {
        if (hasSetUp()) {
            saveService.deleteAll();
        }
    }

    @Test
    public void testCanSaveSearch() {
        findService.search("queen");

        saveService.saveCurrentAs("save me", SearchType.QUERY);

        SearchTab currentTab = getElementFactory().getSearchTabBar().currentTab();
        assertThat(currentTab.getTitle(), is("save me"));
        assertThat(currentTab.getTitle(), not(containsString("New Search")));
        assertThat(currentTab.isNew(), is(false));
    }

}
