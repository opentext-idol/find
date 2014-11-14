package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;

import static org.hamcrest.MatcherAssert.assertThat;

public class SearchPageITCase extends ABCTestBase {
	public SearchPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private SearchPage searchPage;
	private TopNavBar topNavBar;

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = body.getSearchPage();
	}

	@Test
	public void testUnmodifiedResultsToggleButton(){
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Show unmodified results"));

		searchPage.showHideUnmodifiedResults().click();
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Showing unmodified results"));

		searchPage.showHideUnmodifiedResults().click();
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Show unmodified results"));
	}

	@Test
	public void testSearch(){
		topNavBar.search("dog");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("Results for: \"dog\""));

		topNavBar.search("cat");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("Results for: \"cat\""));

		topNavBar.search("ElEPhanT");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("Results for: \"ElEPhanT\""));
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
		assertThat("Back a page button is not disabled", searchPage.getParent(searchPage.backPageButton()).getAttribute("class").contains("disabled"));

		searchPage.forwardPageButton().click();
		assertThat("Back to first page button is not enabled", !searchPage.getParent(searchPage.backToFirstPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Back a page button is not enabled", !searchPage.getParent(searchPage.backPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Page 2 is not active", searchPage.isPageActive(2));

		searchPage.forwardPageButton().click();
		searchPage.forwardPageButton().click();
		searchPage.backPageButton().click();
		assertThat("Page 3 is not active", searchPage.isPageActive(3));

		searchPage.backToFirstPageButton().click();
		assertThat("Page 1 is not active", searchPage.isPageActive(1));

		searchPage.forwardToLastPageButton().click();
		assertThat("Forward to last page button is not disabled", searchPage.getParent(searchPage.forwardToLastPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward a page button is not disabled", searchPage.getParent(searchPage.forwardPageButton()).getAttribute("class").contains("disabled"));

		final int numberOfPages = searchPage.getCurrentPageNumber();

		for (int i = numberOfPages - 1; i > 0; i--) {
			searchPage.backPageButton().click();
			assertThat("Page " + String.valueOf(i) + " is not active", searchPage.isPageActive(i));
		}

		for (int j = 2; j < numberOfPages + 1; j++) {
			searchPage.forwardPageButton().click();
			assertThat("Page " + String.valueOf(j) + " is not active", searchPage.isPageActive(j));
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
}
