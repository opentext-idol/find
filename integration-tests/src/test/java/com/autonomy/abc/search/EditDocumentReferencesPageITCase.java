package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import com.autonomy.abc.selenium.search.SearchFilter;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;

public class EditDocumentReferencesPageITCase extends ABCTestBase {

    public EditDocumentReferencesPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
        super(config, browser, appType, platform);
    }

    private PromotionsPage promotionsPage;
    private SearchPage searchPage;
    private PromotionsDetailPage promotionsDetailPage;
    private EditDocumentReferencesPage editReferencesPage;
    private PromotionActionFactory promotionActionFactory;
    private SearchActionFactory searchActionFactory;
    // simpsons filter avoids duplicate titles
    private SearchFilter globalSearchFilter = new IndexFilter("simpsons");

    @Before
    public void setUp() throws MalformedURLException {
        promotionActionFactory = new PromotionActionFactory(getApplication(), getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
        promotionActionFactory.makeDeleteAll().apply();
        promotionsPage = getElementFactory().getPromotionsPage();
    }

    private List<String> setUpPromotion(final String searchTerm, final String trigger, final int numberOfDocs) {
        Search search = searchActionFactory.makeSearch(searchTerm).applyFilter(globalSearchFilter);
        final List<String> promotedDocTitles = promotionActionFactory.makeCreatePromotion(new SpotlightPromotion(trigger), search, numberOfDocs).apply();
        promotionsDetailPage = promotionActionFactory.goToDetails(trigger.split(" ")[0]).apply();
        promotionsDetailPage.addMoreButton().click();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        return promotedDocTitles;
    }

    // TODO: search action should return a SearchBase?
    private void editDocumentSearch(final String searchTerm) {
        body.getTopNavBar().search(searchTerm);
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        globalSearchFilter.apply(editReferencesPage);
        editReferencesPage.waitForSearchLoadIndicatorToDisappear();
    }

    @Test
    public void testNoMixUpBetweenSearchBucketAndEditPromotionsBucket() {
        final List<String> originalPromotedDocs = setUpPromotion("luke", "jedi goodGuy", 8);
        editReferencesPage.backPageButton().click();
        final List<String>  promotedDocs = promotionsDetailPage.getPromotedTitles();
        verifyThat(promotedDocs.size(), is(originalPromotedDocs.size()));

        promotionsDetailPage.addMoreButton().click();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        final List<String> promotionsBucketList = editReferencesPage.promotionsBucketList();

        verifyThat(promotionsBucketList.size(), is(promotedDocs.size()));
        for (final String docTitle : promotionsBucketList) {
            verifyThat(promotedDocs, hasItem(docTitle));
        }

        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        searchPage = searchActionFactory.makeSearch("edit").apply();
        searchPage.promoteTheseDocumentsButton().click();
        searchPage.addToBucket(3);

        final List<String> searchBucketDocs = searchPage.promotionsBucketList();

        promotionsDetailPage = promotionActionFactory.goToDetails("jedi").apply();
        promotionsDetailPage.addMoreButton().click();
        promotionsPage.loadOrFadeWait();

        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        final List<String> secondPromotionsBucketList = editReferencesPage.promotionsBucketList();
        verifyThat(secondPromotionsBucketList.size(), is(promotionsBucketList.size()));
        for (final String searchBucketDoc : searchBucketDocs) {
            verifyThat(secondPromotionsBucketList, not(hasItem(searchBucketDoc)));
        }

        editDocumentSearch("wall");
        editReferencesPage.searchResultCheckbox(1).click();
        editReferencesPage.searchResultCheckbox(2).click();

        final List<String> finalPromotionsBucketList = editReferencesPage.promotionsBucketList();

        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        searchPage = searchActionFactory.makeSearch("fast").apply();
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

        editDocumentSearch("star");

        for (int i = 1; i < 7; i++) {
            AppElement.scrollIntoView(editReferencesPage.searchResultCheckbox(i), getDriver());
            editReferencesPage.searchResultCheckbox(i).click();
            verifyThat(editReferencesPage.promotionsBucketList().size(), is(i + 4));
        }

        for (int j = 6; j > 0; j--) {
            AppElement.scrollIntoView(editReferencesPage.searchResultCheckbox(j), getDriver());
            editReferencesPage.searchResultCheckbox(j).click();
            verifyThat(editReferencesPage.promotionsBucketList().size(), is(j - 1 + 4));
        }
    }

    @Test
    public void testRefreshEditPromotionPage() throws InterruptedException {
        setUpPromotion("Luke", "Jedi Master", 1);

        getDriver().navigate().refresh();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();

        verifyThat(editReferencesPage.promotionsBucket(), containsText("Save"));
        verifyThat(editReferencesPage.promotionsBucketItems(), not(empty()));
    }

    @Test
    public void testErrorMessageOnStartUpEditReferencesPage() {
        setUpPromotion("Luke", "Jedi Master", 1);

        verifyThat(editReferencesPage, not(containsText("An unknown error occurred executing the search action")));
        verifyThat(editReferencesPage, containsText("Search for new items to promote"));
        verifyThat(editReferencesPage.saveButton(), containsText("Save"));
    }

    @Test //TODO
    public void testEditDocumentReferencesCancel() {
        String originalDoc = setUpPromotion("house", "home", 1).get(0);

        editReferencesPage.deleteDocFromWithinBucket(originalDoc);
        editDocumentSearch("mansion");
        editReferencesPage.searchResultCheckbox(1).click();
        editReferencesPage.searchResultCheckbox(2).click();
        editReferencesPage.javascriptClick(editReferencesPage.forwardPageButton());
        editReferencesPage.searchResultCheckbox(3).click();
        editReferencesPage.searchResultCheckbox(4).click();

        editDocumentSearch("cottage");
        verifyThat(editReferencesPage.getCurrentPageNumber(), is(1));
        editReferencesPage.searchResultCheckbox(5).click();
        editReferencesPage.searchResultCheckbox(6).click();
        editReferencesPage.cancelButton().click();

        verifyThat(promotionsDetailPage.getPromotedTitles(), hasSize(1));
        verifyThat(promotionsDetailPage.getPromotedTitles(), hasItem(originalDoc));

        promotionsDetailPage.addMoreButton().click();
        editReferencesPage = getElementFactory().getEditDocumentReferencesPage();
        verifyThat(editReferencesPage.promotionsBucketList(), contains(originalDoc));
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
        editReferencesPage.tryClickThenTryParentClick(editReferencesPage.saveButton());
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions/edit"));
    }

    private void checkDocumentViewable(String title) {
        final String handle = getDriver().getWindowHandle();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());

        getDriver().switchTo().frame(docViewer.frame());
        verifyThat(getDriver().findElement(By.xpath(".//*")), containsText(title));

        getDriver().switchTo().window(handle);
        docViewer.close();
    }

    @Test
    public void testViewFromBucketAndFromSearchResults() throws InterruptedException {
        setUpPromotion("apple", "potato", 35);

        for (int i = 0; i < 5; i++){
            final String docTitle = editReferencesPage.promotionsBucketWebElements().get(i).getText();
            editReferencesPage.getPromotionBucketElementByTitle(docTitle).click();
            checkDocumentViewable(docTitle);
        }

        editDocumentSearch("fox");
        verifyThat(editReferencesPage, not(containsText("No results found...")));

        for (int j = 1; j <= 2; j++) {
            for (int i = 1; i <= 5; i++) {
                final String searchResultTitle = editReferencesPage.getSearchResultTitle(i);
                editReferencesPage.getSearchResult(i).click();
                checkDocumentViewable(searchResultTitle);
            }

            editReferencesPage.javascriptClick(editReferencesPage.forwardPageButton());
            editReferencesPage.loadOrFadeWait();
        }

        editReferencesPage.emptyBucket();
        editDocumentSearch("banana");

        for (int i = 1; i < 5; i++) {
            editReferencesPage.searchResultCheckbox(i).click();
            final String docTitle = editReferencesPage.getSearchResultTitle(i);
            editReferencesPage.getPromotionBucketElementByTitle(docTitle).click();
            checkDocumentViewable(docTitle);
        }
    }

    @Test
    public void testCheckboxUpdatesWithBucketDelete() {
        setUpPromotion("marshmallow", "white fluffy", 4);
        editDocumentSearch("marshmallow");
        final List<String> bucketList = editReferencesPage.promotionsBucketList();
        verifyThat(bucketList, hasSize(4));

        for (final String docTitle : bucketList) {
            verifyThat("search result checkbox is initially checked", editReferencesPage.searchCheckboxForTitle(docTitle).isChecked(), is(true));
            editReferencesPage.deleteDocFromWithinBucket(docTitle);
            verifyThat("search result checkbox is unchecked after removing", editReferencesPage.searchCheckboxForTitle(docTitle).isChecked(), is(false));
            verifyThat(editReferencesPage.promotionsBucketList(), not(contains(docTitle)));
        }

        verifyThat(editReferencesPage.saveButton(), disabled());
        editReferencesPage.tryClickThenTryParentClick(editReferencesPage.saveButton());
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions/edit"));

        editReferencesPage.searchResultCheckbox(6).click();
        final String newPromotedDoc = editReferencesPage.getSearchResultTitle(6);

        editReferencesPage.tryClickThenTryParentClick(editReferencesPage.saveButton());
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions/detail"));

        verifyThat(promotionsDetailPage.getPromotedTitles(), hasItem(newPromotedDoc));
        verifyThat(promotionsDetailPage.getPromotedTitles(), hasSize(1));
    }
}
