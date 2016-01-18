package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.core.Is.is;

//CSA-1564
public class IndexSetUpITCase extends HostedTestBase {
    private IndexService indexService;
    private PromotionService<?> promotionService;

    private final Index index;
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexSetUpITCase.class);

    public IndexSetUpITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));

        String indexName = UUID.randomUUID().toString().replace('-','a');
        index = new Index(indexName);
    }

    @Before
    public void setUp(){
        indexService = new IndexService(getApplication(), getElementFactory());
        promotionService = getApplication().createPromotionService(getElementFactory());

        LOGGER.info("Will create index " + index);
        indexService.setUpIndex(index);
    }

    @Test
    public void testIndexSetUp() {
        final String site = "www.bbc.co.uk";
        final String trigger = "trigger";

        verifyDetails();
        addToIndex(site);
        String promotedTitle = promoteDocumentInIndex(trigger);
        verifyPromotedDocument(promotedTitle);
    }

    private void verifyDetails() {
        IndexesDetailPage detailPage = indexService.goToDetails(index);
        verifyThat(detailPage.getIndexHeader(), is(index.getName()));
        verifyThat(detailPage.getIndexTitle(), is(index.getName()));
        verifyThat(detailPage.getCreatedDate(), is(new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new Date())));
    }

    private void addToIndex(String site) {
        IndexesDetailPage detailPage = indexService.goToDetails(index);
        LOGGER.info("Adding site " + site + " to index");
        detailPage.addSiteToIndex(site);
        detailPage.waitForSiteToIndex(site);
    }

    private String promoteDocumentInIndex(String trigger) {
        SearchQuery search = new SearchQuery("*").withFilter(new IndexFilter(index));
        Promotion promotion = new SpotlightPromotion(trigger);
        return promotionService.setUpPromotion(promotion, search, 1).get(0);
    }

    private void verifyPromotedDocument(String promotedTitle) {
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedTitle));
        verifyThat(searchPage.promotedResult(1), containsText("Index: " + index.getName()));
    }

    @After
    public void deletePromotions() {
        promotionService.deleteAll();
    }

    @After
    public void deleteIndex() {
        indexService.deleteIndex(index);
    }
}
