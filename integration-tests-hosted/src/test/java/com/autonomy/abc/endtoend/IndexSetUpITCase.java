package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.HSOApplication;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.core.Is.is;

//CSA-1564
// TODO: re-enable once
public class IndexSetUpITCase extends ABCTestBase {
    private IndexService indexService;
    private PromotionService promotionService;
    private SearchActionFactory searchActionFactory;
    private final String indexName;
    private final Index index;
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexSetUpITCase.class);

    public IndexSetUpITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);

        indexName = UUID.randomUUID().toString().replace('-','a');
        index = new Index(indexName);
    }

    @Override
    public HSOApplication getApplication() {
        return (HSOApplication) super.getApplication();
    }

    @Override
    public HSOElementFactory getElementFactory() {
        return (HSOElementFactory) super.getElementFactory();
    }

    @Before
    public void setUp(){
        indexService = new IndexService(getApplication(), getElementFactory());
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());

        LOGGER.info("Will create index " + indexName);
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
        verifyThat(detailPage.getIndexHeader(), is(indexName));
        verifyThat(detailPage.getIndexTitle(), is(indexName));
        verifyThat(detailPage.getCreatedDate(), is(new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new Date())));
    }

    private void addToIndex(String site) {
        IndexesDetailPage detailPage = indexService.goToDetails(index);
        LOGGER.info("Adding site " + site + " to index");
        detailPage.addSiteToIndex(site);
        detailPage.waitForSiteToIndex(site);
    }

    private String promoteDocumentInIndex(String trigger) {
        Search search = searchActionFactory.makeSearch("*")
                .applyFilter(new IndexFilter(index.getName()));
        Promotion promotion = new SpotlightPromotion(trigger);
        return promotionService.setUpPromotion(promotion, search, 1).get(0);
    }

    private void verifyPromotedDocument(String promotedTitle) {
        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedTitle));
        verifyThat(searchPage.getPromotionBucketElementByTitle(promotedTitle), containsText("Index: " + indexName));
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
