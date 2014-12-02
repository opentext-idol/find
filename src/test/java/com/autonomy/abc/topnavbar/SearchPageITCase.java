package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.PromotionsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;

import static org.hamcrest.MatcherAssert.assertThat;

public class SearchPageITCase extends ABCTestBase {
	public SearchPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private SearchPage searchPage;
	private TopNavBar topNavBar;
	private CreateNewPromotionsPage createPromotionsPage;
	private PromotionsPage promotionsPage;

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = body.getSearchPage();
	}

	@Test
	public void testUnmodifiedResultsToggleButton(){
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Show unmodified results"));
		assertThat("Url incorrect", getDriver().getCurrentUrl().contains("/modified"));

		searchPage.showHideUnmodifiedResults().click();
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Showing unmodified results"));
		assertThat("Url incorrect", getDriver().getCurrentUrl().contains("/unmodified"));

		searchPage.showHideUnmodifiedResults().click();
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Show unmodified results"));
		assertThat("Url incorrect", getDriver().getCurrentUrl().contains("/modified"));
	}

	@Test
	public void testSearch(){
		topNavBar.search("dog");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("dog"));

		topNavBar.search("cat");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("cat"));

		topNavBar.search("ElEPhanT");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("ElEPhanT"));
	}

	@Test
	public void testPromoteButton(){
		searchPage.promoteButton().click();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be visible", !searchPage.promoteTheseItemsButton().isDisplayed());
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount() == 0);

		searchPage.searchResultCheckbox(1).click();
		assertThat("Promote these items button should be visible", searchPage.promoteTheseItemsButton().isDisplayed());
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 1);

		searchPage.promotionsBucketClose();
		assertThat("Promoted items bucket has not appeared", !searchPage.getText().contains("Select Items to Promote"));

		searchPage.promoteButton().click();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be visible", !searchPage.promoteTheseItemsButton().isDisplayed());
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount() == 0);
	}

	@Test
	public void testAddFilesToPromoteBucket() {
		searchPage.promoteButton().click();

		for (int i = 1; i < 7; i++) {
			AppElement.scrollIntoView(searchPage.searchResultCheckbox(i), getDriver());
			searchPage.searchResultCheckbox(i).click();
			assertThat("Promoted items count should equal " + String.valueOf(i), searchPage.promotedItemsCount() == i);
		}

		for (int j = 6; j > 0; j--) {
			AppElement.scrollIntoView(searchPage.searchResultCheckbox(j), getDriver());
			searchPage.searchResultCheckbox(j).click();
			assertThat("Promoted items count should equal " + String.valueOf(j), searchPage.promotedItemsCount() == j - 1);
		}

		searchPage.promotionsBucketClose();
	}

	@Test
	public void testSearchResultsPagination() {
		topNavBar.search("dog");
		AppElement.scrollIntoView(searchPage.backToFirstPageButton(), getDriver());
		assertThat("Back to first page button is not disabled", searchPage.isBackToFirstPageButtonDisabled());
		assertThat("Back a page button is not disabled", AppElement.getParent(searchPage.backPageButton()).getAttribute("class").contains("disabled"));

		searchPage.forwardPageButton().click();
		assertThat("Back to first page button is not enabled", !AppElement.getParent(searchPage.backToFirstPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Back a page button is not enabled", !AppElement.getParent(searchPage.backPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Page 2 is not active", searchPage.isPageActive(2));

		searchPage.forwardPageButton().click();
		searchPage.forwardPageButton().click();
		searchPage.backPageButton().click();
		assertThat("Page 3 is not active", searchPage.isPageActive(3));

		searchPage.backToFirstPageButton().click();
		assertThat("Page 1 is not active", searchPage.isPageActive(1));

		searchPage.forwardToLastPageButton().click();
		assertThat("Forward to last page button is not disabled", AppElement.getParent(searchPage.forwardToLastPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward a page button is not disabled", AppElement.getParent(searchPage.forwardPageButton()).getAttribute("class").contains("disabled"));

		final int numberOfPages = searchPage.getCurrentPageNumber();

		for (int i = numberOfPages - 1; i > 0; i--) {
			searchPage.backPageButton().click();
			assertThat("Page " + String.valueOf(i) + " is not active", searchPage.isPageActive(i));
			assertThat("Url incorrect", getDriver().getCurrentUrl().endsWith(String.valueOf(i)));
		}

		for (int j = 2; j < numberOfPages + 1; j++) {
			searchPage.forwardPageButton().click();
			assertThat("Page " + String.valueOf(j) + " is not active", searchPage.isPageActive(j));
			assertThat("Url incorrect", getDriver().getCurrentUrl().endsWith(String.valueOf(j)));
		}
	}

	@Test
	public void testAddDocumentToPromotionsBucket() {
		topNavBar.search("horse");
		searchPage.promoteButton().click();
		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 1);
		assertThat("File in bucket description does not match file added", searchPage.getSearchResultTitle(1).equals(searchPage.bucketDocumentTitle(1)));
	}

	@Test
	public void testPromoteTheseItemsButtonLink() {
		topNavBar.search("fox");
		searchPage.promoteButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.promoteTheseItemsButton().click();
		assertThat("Create new promotions page not open", getDriver().getCurrentUrl().endsWith("promotions/create"));
	}

	@Test
	public void testMultiDocPromotionDrawerExpandAndPagination() {
		topNavBar.search("sail");
		searchPage.createAMultiDocumentPromotion(18);
		createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "boat");

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));

		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("boat").click();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		promotionsPage.clickableSearchTrigger("boat").click();

		assertThat("Promotions found label is incorrect", searchPage.promotionsLabel().getText().contains("18"));
		assertThat("Summary size should equal 2", searchPage.getPromotionSummarySize() == 2);

		searchPage.showMoreButton();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 5);

		searchPage.showLessButton();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 2);

		searchPage.showMoreButton();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 5);

		assertThat("Back to start button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));
		assertThat("Back button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward to end button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryForwardButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back to start button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));
		assertThat("Back button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward to end button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryForwardButton().click();
		searchPage.loadOrFadeWait();
		searchPage.promotionSummaryForwardButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back to start button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));
		assertThat("Back button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward button should be disabled", AppElement.getParent(searchPage.promotionSummaryForwardButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward to end button should be disabled", AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryBackButton().click();
		searchPage.loadOrFadeWait();
		searchPage.promotionSummaryBackButton().click();
		searchPage.loadOrFadeWait();
		searchPage.promotionSummaryBackButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back to start button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryForwardToEndButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Forward to end button should be disabled", AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryBackToStartButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));

		body.getSideNavBar().getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testDocumentsRemainInBucket() {
		topNavBar.search("cow");
		searchPage.promoteButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.searchResultCheckbox(2).click();
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount() == 2);

		topNavBar.search("bull");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount() == 2);
		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 3);

		topNavBar.search("cow");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount() == 3);

		topNavBar.search("bull");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 3);

		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 2);

		topNavBar.search("cow");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 2);
	}

	@Test
	public void testWhitespaceSearch() {
		topNavBar.search(" ");
		assertThat("Whitespace search should not return a message as if it is a blacklisted term", !searchPage.getText().contains("All search terms are blacklisted"));
	}
}
