package com.autonomy.abc.search;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.element.Pagination;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class EditDocumentReferencesPageITCase extends HybridIsoTestBase {

    public EditDocumentReferencesPageITCase(final TestConfig config) {
        super(config);
    }

    private PromotionsDetailPage promotionsDetailPage;
    private EditDocumentReferencesPage editReferencesPage;
    private PromotionService<?> promotionService;
    private SearchService searchService;

    @Before
    public void setUp() throws MalformedURLException {
        promotionService = getApplication().promotionService();
        searchService = getApplication().searchService();

        promotionService.deleteAll();
    }

    private List<String> setUpPromotion(final String searchTerm, final String trigger, final int numberOfDocs) {
        final List<String> promotedDocTitles = promotionService.setUpPromotion(new SpotlightPromotion(trigger), new Query(searchTerm).withFilter(new LanguageFilter(Language.ENGLISH)), numberOfDocs);
        promotionsDetailPage = promotionService.goToDetails(trigger.split(" ")[0]);
        promotionsDetailPage.addMoreButton().click();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        return promotedDocTitles;
    }

    // TODO: search action should return a SearchBase?
    private void editDocumentSearch(final String searchTerm) {
        getElementFactory().getTopNavBar().search(searchTerm);
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        editReferencesPage.waitForSearchLoadIndicatorToDisappear();
    }

    @Test
    public void testNoMixUpBetweenSearchBucketAndEditPromotionsBucket() {
        final List<String> originalPromotedDocs = setUpPromotion("luke", "jedi goodGuy", 8);
        editReferencesPage.cancelButton().click();
        final List<String>  promotedDocs = promotionsDetailPage.getPromotedTitles();
        verifyThat(promotedDocs.size(), is(originalPromotedDocs.size()));

        promotionsDetailPage.addMoreButton().click();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        final List<String> promotionsBucketList = editReferencesPage.getBucketTitles();

        for (final String docTitle : promotionsBucketList) {
            verifyThat(promotedDocs, hasItem(equalToIgnoringCase(docTitle)));
        }

        getApplication().switchTo(KeywordsPage.class);
        SearchPage searchPage = searchService.search("edit");
        searchPage.openPromotionsBucket();
        searchPage.addDocsToBucket(3);

        final List<String> searchBucketDocs = searchPage.getBucketTitles();

        promotionsDetailPage = promotionService.goToDetails("jedi");
        promotionsDetailPage.addMoreButton().click();
        Waits.loadOrFadeWait();

        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        final List<String> secondPromotionsBucketList = editReferencesPage.getBucketTitles();
        verifyThat(secondPromotionsBucketList.size(), is(promotionsBucketList.size()));
        for (final String searchBucketDoc : searchBucketDocs) {
            verifyThat(secondPromotionsBucketList, not(hasItem(equalToIgnoringCase(searchBucketDoc))));
        }

        editDocumentSearch("wall");
        editReferencesPage.addDocsToBucket(2);

        final List<String> finalPromotionsBucketList = editReferencesPage.getBucketTitles();

        getApplication().switchTo(KeywordsPage.class);
        searchPage = searchService.search("fast");
        searchPage.openPromotionsBucket();

        final List<String> searchPageBucketDocs = searchPage.getBucketTitles();

        for (final String bucketDoc : finalPromotionsBucketList) {
            verifyThat(searchPageBucketDocs, not(hasItem(bucketDoc)));
        }
    }

    @Test
    public void testAddRemoveDocsToEditBucket() {
        setUpPromotion("yoda", "green dude", 4);
        int currentSize = editReferencesPage.getBucketTitles().size();

        editDocumentSearch("unrelated");

        for (int i = 1; i < 7; i++) {
            editReferencesPage.addDocToBucket(i);
            verifyThat(editReferencesPage.getBucketTitles(), hasSize(++currentSize));
        }

        for (int j = 6; j > 0; j--) {
            editReferencesPage.removeDocFromBucket(j);
            verifyThat(editReferencesPage.getBucketTitles(), hasSize(--currentSize));
        }
    }

    @Test
    @ResolvedBug("CSA-1755")
    public void testRefreshEditPromotionPage() throws InterruptedException {
        final String originalDoc = setUpPromotion("Luke", "jedi master", 1).get(0);
        assumeThat(editReferencesPage.getBucketTitles(), not(empty()));
        verifyRefreshing();

        editDocumentSearch("solo");
        editReferencesPage.deleteDocFromWithinBucket(originalDoc);
        editReferencesPage.addDocToBucket(1);
        verifyRefreshing();
    }

    private void verifyRefreshing() {
        getWindow().refresh();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();

        new WebDriverWait(getDriver(), 5)
                .withMessage("Waiting for promotion bucket to have results")
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(final WebDriver driver) {
                        return !editReferencesPage.getBucketTitles().isEmpty();
                    }
                });

        verifyThat(editReferencesPage.saveButton(), not(disabled()));
        verifyThat(editReferencesPage.getBucketTitles(), not(empty()));
    }

    @Test
    public void testErrorMessageOnStartUpEditReferencesPage() {
        setUpPromotion("Luke", "jedi master", 1);

        verifyThat(editReferencesPage, not(containsText("An unknown error occurred executing the search action")));
        verifyThat(editReferencesPage, containsText("Search for new items to promote"));
        verifyThat(editReferencesPage.saveButton(), hasTextThat(equalToIgnoringCase("Save")));
    }

    @Test
    public void testEditDocumentReferencesCancel() {
        final String originalDoc = setUpPromotion("house", "home", 1).get(0);
        assumeThat(editReferencesPage.getBucketTitles(), not(empty()));

        editReferencesPage.deleteDocFromWithinBucket(originalDoc);
        editDocumentSearch("mansion");
        editReferencesPage.addDocToBucket(1);
        editReferencesPage.addDocToBucket(2);
        editReferencesPage.switchResultsPage(Pagination.NEXT);
        editReferencesPage.addDocToBucket(3);
        editReferencesPage.addDocToBucket(4);

        editDocumentSearch("villa");
        verifyThat(editReferencesPage.getCurrentPageNumber(), is(1));
        editReferencesPage.addDocToBucket(5);
        editReferencesPage.addDocToBucket(6);
        DriverUtil.scrollIntoView(getDriver(), editReferencesPage.cancelButton());
        editReferencesPage.cancelButton().click();

        verifyThat(promotionsDetailPage.getPromotedTitles(), hasSize(1));
        verifyThat(promotionsDetailPage.getPromotedTitles(), hasItem(originalDoc));

        promotionsDetailPage.addMoreButton().click();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        verifyThat(editReferencesPage.getBucketTitles(), hasItem(equalToIgnoringCase(originalDoc)));
        verifyThat(editReferencesPage.getBucketTitles(), hasSize(1));
    }

    @Test
    public void testDeleteItemsFromWithinTheBucket() {
        setUpPromotion("cheese", "cheddar brie", 4);
        final List<String> bucketList = editReferencesPage.getBucketTitles();
        assumeThat(bucketList, hasSize(4));
        verifyThat(editReferencesPage.saveButton(), not(disabled()));

        int bucketSize = bucketList.size();
        for (final String bucketDocTitle : bucketList) {
            editReferencesPage.deleteDocFromWithinBucket(bucketDocTitle);
            verifyThat(editReferencesPage.getBucketTitles(), not(hasItem(bucketDocTitle)));
            verifyThat(editReferencesPage.getBucketTitles(), hasSize(--bucketSize));
        }

        verifyThat(editReferencesPage.saveButton(), disabled());
        ElementUtil.tryClickThenTryParentClick(editReferencesPage.saveButton());
        verifyThat(getWindow(), urlContains("promotions/edit"));
    }

    private void checkDocumentViewable(final String title) {
        final DocumentViewer docViewer = DocumentViewer.make(getDriver());
        final Frame frame = new Frame(getWindow(), docViewer.frame());

        verifyThat("document '" + title + "' is viewable", frame.getText(), not(isEmptyOrNullString()));
        docViewer.close();
    }

    @Test
    @ResolvedBug("CCUK-3710")
    public void testViewFromBucketAndFromSearchResults() throws InterruptedException {
        setUpPromotion("apple", "potato", 7);
        if (verifyThat(editReferencesPage.getBucketTitles(), not(empty()))) {
            for (int i = 0; i < 5; i++) {
                final String docTitle = editReferencesPage.getBucketTitles().get(i);
                editReferencesPage.promotionBucketElementByTitle(docTitle).click();
                checkDocumentViewable(docTitle);
            }
        }

        editDocumentSearch("fox");
        verifyThat(editReferencesPage, not(containsText("No results found...")));

        for (int j = 1; j <= 2; j++) {
            for (int i = 1; i <= 5; i++) {
                final String searchResultTitle = editReferencesPage.getSearchResult(i).getTitleString();
                editReferencesPage.getSearchResult(i).title().click();
                checkDocumentViewable(searchResultTitle);
            }
            editReferencesPage.switchResultsPage(Pagination.NEXT);
        }

        editReferencesPage.emptyBucket();
        editDocumentSearch("banana");

        for (int i = 1; i < 5; i++) {
            editReferencesPage.addDocToBucket(i);
            final String docTitle = editReferencesPage.getSearchResult(i).getTitleString();
            DriverUtil.scrollIntoView(getDriver(), editReferencesPage.promotionBucketElementByTitle(docTitle));
            editReferencesPage.promotionBucketElementByTitle(docTitle).click();
            checkDocumentViewable(docTitle);
        }
    }

    @Test
    @ResolvedBug("CSA-1761")
    public void testCheckboxUpdatesWithBucketDelete() {
        setUpPromotion("fred", "white fluffy", 4);
        editDocumentSearch("fred");
        final List<String> bucketList = editReferencesPage.getBucketTitles();
        assumeThat(bucketList, hasSize(4));

        for (final String docTitle : bucketList) {
            verifyThat("search result checkbox is initially checked", editReferencesPage.searchCheckboxForTitle(docTitle).isChecked(), is(true));
            editReferencesPage.deleteDocFromWithinBucket(docTitle);
            verifyThat("search result checkbox is unchecked after removing", editReferencesPage.searchCheckboxForTitle(docTitle).isChecked(), is(false));
            verifyThat(editReferencesPage.getBucketTitles(), not(contains(docTitle)));
        }

        verifyThat(editReferencesPage.saveButton(), disabled());
        ElementUtil.tryClickThenTryParentClick(editReferencesPage.saveButton());
        verifyThat(getWindow(), urlContains("promotions/edit"));

        editReferencesPage.addDocToBucket(6);
        final String newPromotedDoc = editReferencesPage.getSearchResult(6).getTitleString();

        DriverUtil.scrollIntoView(getDriver(), editReferencesPage.saveButton());
        ElementUtil.tryClickThenTryParentClick(editReferencesPage.saveButton());
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        verifyThat(getWindow(), urlContains("promotions/detail"));

        List<String> newTitles = new ArrayList<>();
        try {
            newTitles = promotionsDetailPage.getPromotedTitles();
        } catch (final TimeoutException e) {
            /* due to "Unknown Document" bug */
        }
        verifyThat(newTitles, hasItem(newPromotedDoc));
        verifyThat(newTitles, hasSize(1));
    }


    @Test
    public void testDeletedDocumentsRemainDeleted() {
        setUpPromotion("dog", "woof bark", 8);
        final List<String> bucketList = editReferencesPage.getBucketTitles();
        assumeThat(bucketList, hasSize(8));

        for (int i = 0; i < 4; i++) {
            editReferencesPage.deleteDocFromWithinBucket(bucketList.get(i));
            verifyThat(editReferencesPage.getBucketTitles(), hasSize(7 - i));
        }

        editReferencesPage.saveButton().click();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        final List<String> promotionsList = promotionsDetailPage.getPromotedTitles();

        for (int i = 0; i < 4; i++) {
            verifyThat(promotionsList, not(hasItem(equalToIgnoringCase(bucketList.get(i)))));
            verifyThat(promotionsList, hasItem(equalToIgnoringCase(bucketList.get(i + 4))));
        }
    }

    @Test
    @ResolvedBug({"CSA-1761", "CCUK-3710", "CCUK-3728"})
    public void testAddedDocumentsNotUnknown(){
        setUpPromotion("smiles", "fun happiness", 2);

        editDocumentSearch("Friday");

        editReferencesPage.addDocToBucket(5);
        final String title = editReferencesPage.getSearchResult(5).getTitleString();
        DriverUtil.scrollIntoView(getDriver(), editReferencesPage.saveButton());
        editReferencesPage.saveButton().click();

        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();

        final List<String> promotedTitles = promotionsDetailPage.getPromotedTitles();
        verifyThat(promotedTitles, not(hasItem("Unknown Document")));

        if (verifyThat(promotedTitles, hasItem(title))) {
            promotionsDetailPage.viewDocument(title);
            SharedPreviewTests.testDocumentPreview(getMainSession(), DocumentViewer.make(getDriver()));
        }
    }


}
