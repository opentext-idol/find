package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.element.Pagination;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.search.DocumentViewer;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class EditDocumentReferencesPageITCase extends ABCTestBase {

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
        final List<String> promotedDocTitles = promotionService.setUpPromotion(new SpotlightPromotion(trigger), searchTerm, numberOfDocs);
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
        final List<String> promotionsBucketList = editReferencesPage.promotionsBucketList();

        verifyThat(promotionsBucketList.size(), is(promotedDocs.size()));
        for (final String docTitle : promotionsBucketList) {
            verifyThat(promotedDocs, hasItem(equalToIgnoringCase(docTitle)));
        }

        getApplication().switchTo(KeywordsPage.class);
        SearchPage searchPage = searchService.search("edit");
        searchPage.promoteTheseDocumentsButton().click();
        searchPage.addToBucket(3);

        final List<String> searchBucketDocs = searchPage.promotionsBucketList();

        promotionsDetailPage = promotionService.goToDetails("jedi");
        promotionsDetailPage.addMoreButton().click();
        Waits.loadOrFadeWait();

        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        final List<String> secondPromotionsBucketList = editReferencesPage.promotionsBucketList();
        verifyThat(secondPromotionsBucketList.size(), is(promotionsBucketList.size()));
        for (final String searchBucketDoc : searchBucketDocs) {
            verifyThat(secondPromotionsBucketList, not(hasItem(equalToIgnoringCase(searchBucketDoc))));
        }

        editDocumentSearch("wall");
        editReferencesPage.searchResultCheckbox(1).click();
        editReferencesPage.searchResultCheckbox(2).click();

        final List<String> finalPromotionsBucketList = editReferencesPage.promotionsBucketList();

        getApplication().switchTo(KeywordsPage.class);
        searchPage = searchService.search("fast");
        searchPage.promoteTheseDocumentsButton().click();

        final List<String> searchPageBucketDocs = searchPage.promotionsBucketList();

        for (final String bucketDoc : finalPromotionsBucketList) {
            verifyThat(searchPageBucketDocs, not(hasItem(bucketDoc)));
        }
    }

    @Test
    public void testAddRemoveDocsToEditBucket() {
        final int promotedCount = 4;
        setUpPromotion("yoda", "green dude", promotedCount);
        verifyThat(editReferencesPage.promotionsBucketList().size(), is(promotedCount));

        editDocumentSearch("unrelated");

        for (int i = 1; i < 7; i++) {
            ElementUtil.scrollIntoView(editReferencesPage.searchResultCheckbox(i), getDriver());
            editReferencesPage.searchResultCheckbox(i).click();
            verifyThat(editReferencesPage.promotionsBucketList().size(), is(i + 4));
        }

        for (int j = 6; j > 0; j--) {
            ElementUtil.scrollIntoView(editReferencesPage.searchResultCheckbox(j), getDriver());
            editReferencesPage.searchResultCheckbox(j).click();
            verifyThat(editReferencesPage.promotionsBucketList().size(), is(j - 1 + 4));
        }
    }

    @Test
    @KnownBug("CSA-1755")
    public void testRefreshEditPromotionPage() throws InterruptedException {
        String originalDoc = setUpPromotion("Luke", "jedi master", 1).get(0);
        verifyRefreshing();

        editDocumentSearch("solo");
        editReferencesPage.deleteDocFromWithinBucket(originalDoc);
        editReferencesPage.searchResultCheckbox(1).click();
        verifyRefreshing();
    }

    private void verifyRefreshing() {
        getDriver().navigate().refresh();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        verifyThat(editReferencesPage.saveButton(), not(disabled()));
        verifyThat(editReferencesPage.promotionsBucketItems(), not(empty()));
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
        String originalDoc = setUpPromotion("house", "home", 1).get(0);

        editReferencesPage.deleteDocFromWithinBucket(originalDoc);
        editDocumentSearch("mansion");
        editReferencesPage.searchResultCheckbox(1).click();
        editReferencesPage.searchResultCheckbox(2).click();
        editReferencesPage.switchResultsPage(Pagination.NEXT);
        editReferencesPage.searchResultCheckbox(3).click();
        editReferencesPage.searchResultCheckbox(4).click();

        editDocumentSearch("villa");
        verifyThat(editReferencesPage.getCurrentPageNumber(), is(1));
        editReferencesPage.searchResultCheckbox(5).click();
        editReferencesPage.searchResultCheckbox(6).click();
        editReferencesPage.cancelButton().click();

        verifyThat(promotionsDetailPage.getPromotedTitles(), hasSize(1));
        verifyThat(promotionsDetailPage.getPromotedTitles(), hasItem(originalDoc));

        promotionsDetailPage.addMoreButton().click();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        verifyThat(editReferencesPage.promotionsBucketList(), hasItem(equalToIgnoringCase(originalDoc)));
        verifyThat(editReferencesPage.promotionsBucketList(), hasSize(1));
    }

    @Test
    public void testDeleteItemsFromWithinTheBucket() {
        setUpPromotion("cheese", "cheddar brie", 4);
        final List<String> bucketList = editReferencesPage.promotionsBucketList();
        verifyThat(bucketList, hasSize(4));
        verifyThat(editReferencesPage.saveButton(), not(disabled()));


        for (final String bucketDocTitle : bucketList) {
            final int docIndex = bucketList.indexOf(bucketDocTitle);
            editReferencesPage.deleteDocFromWithinBucket(bucketDocTitle);
            verifyThat(editReferencesPage.promotionsBucketList(), not(hasItem(bucketDocTitle)));
            verifyThat(editReferencesPage.promotionsBucketList(), hasSize(3 - docIndex));
        }

        verifyThat(editReferencesPage.saveButton(), disabled());
        ElementUtil.tryClickThenTryParentClick(editReferencesPage.saveButton());
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions/edit"));
    }

    private void checkDocumentViewable(String title) {
        final String handle = getDriver().getWindowHandle();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());

        getDriver().switchTo().frame(docViewer.frame());
        verifyThat("document '" + title + "' is viewable", getDriver().findElement(By.xpath(".//*")), not(hasTextThat(isEmptyOrNullString())));

        getDriver().switchTo().window(handle);
        docViewer.close();
    }

    @Test
    public void testViewFromBucketAndFromSearchResults() throws InterruptedException {
        setUpPromotion("apple", "potato", 35);

        for (int i = 0; i < 5; i++){
            final String docTitle = editReferencesPage.promotionsBucketWebElements().get(i).getText();
            editReferencesPage.promotionBucketElementByTitle(docTitle).click();
            checkDocumentViewable(docTitle);
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
            editReferencesPage.searchResultCheckbox(i).click();
            final String docTitle = editReferencesPage.getSearchResult(i).getTitleString();
            editReferencesPage.promotionBucketElementByTitle(docTitle).click();
            checkDocumentViewable(docTitle);
        }
    }

    @Test
    @KnownBug("CSA-1761")
    public void testCheckboxUpdatesWithBucketDelete() {
        setUpPromotion("fred", "white fluffy", 4);
        editDocumentSearch("fred");
        final List<String> bucketList = editReferencesPage.promotionsBucketList();
        verifyThat(bucketList, hasSize(4));

        for (final String docTitle : bucketList) {
            verifyThat("search result checkbox is initially checked", editReferencesPage.searchCheckboxForTitle(docTitle).isChecked(), is(true));
            editReferencesPage.deleteDocFromWithinBucket(docTitle);
            verifyThat("search result checkbox is unchecked after removing", editReferencesPage.searchCheckboxForTitle(docTitle).isChecked(), is(false));
            verifyThat(editReferencesPage.promotionsBucketList(), not(contains(docTitle)));
        }

        verifyThat(editReferencesPage.saveButton(), disabled());
        ElementUtil.tryClickThenTryParentClick(editReferencesPage.saveButton());
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions/edit"));

        editReferencesPage.searchResultCheckbox(6).click();
        final String newPromotedDoc = editReferencesPage.getSearchResult(6).getTitleString();

        ElementUtil.tryClickThenTryParentClick(editReferencesPage.saveButton());
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions/detail"));

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
        final List<String> bucketList = editReferencesPage.promotionsBucketList();
        verifyThat(bucketList, hasSize(8));

        for (int i = 0; i < 4; i++) {
            editReferencesPage.deleteDocFromWithinBucket(bucketList.get(i));
            verifyThat(editReferencesPage.promotionsBucketList(), hasSize(7 - i));
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
    @KnownBug("CSA-1761")
    public void testAddedDocumentsNotUnknown(){
        setUpPromotion("smiles", "fun happiness", 2);

        editDocumentSearch("Friday");

        String title = editReferencesPage.getSearchResult(5).getTitleString();
        editReferencesPage.searchResultCheckbox(5).click();

        editReferencesPage.saveButton().click();

        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();

        List<String> promotedTitles = promotionsDetailPage.getPromotedTitles();

        verifyThat(promotedTitles, hasItem(title));
        verifyThat(promotedTitles, not(hasItem("Unknown Document")));

        promotionsDetailPage.viewDocument(title);

        new WebDriverWait(getDriver(),30).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#cboxLoadedContent .icon-spin")));

        verifyThat(getDriver().findElement(By.id("cboxLoadedContent")), displayed());
        verifyThat(getDriver().findElement(By.id("cboxLoadedContent")), not(containsText("500")));
    }
}
