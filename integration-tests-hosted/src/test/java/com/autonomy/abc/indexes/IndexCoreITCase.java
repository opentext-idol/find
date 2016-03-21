package com.autonomy.abc.indexes;

import com.autonomy.abc.base.HSODTearDown;
import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.autonomy.abc.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Category(CoreFeature.class)
public class IndexCoreITCase extends HostedTestBase {
    private IndexService indexService;

    public IndexCoreITCase(TestConfig config) {
        super(config);
        useIndexTestsUser();
    }

    @Before
    public void setUp() {
        indexService = getApplication().indexService();
    }

    @After
    public void tearDown() {
        HSODTearDown.INDEXES.tearDown(this);
    }

    @Test
    public void testCreateIndex() {
        Index index = new Index("something");
        indexService.setUpIndex(index);
        assertThat(indexService.goToDetails(index).getIndexTitle(), is(index.getName()));
    }

    @Test
    public void testDeleteIndex() {
        Index index = new Index("delete");

        indexService.setUpIndex(index);
        assertThat(indexService.goToIndexes().getIndexDisplayNames(), hasItem(index.getName()));

        indexService.deleteIndex(index);
        assertThat(indexService.goToIndexes().getIndexDisplayNames(), not(hasItem(index.getName())));
    }
}
