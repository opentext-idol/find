package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsElement;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.PromotionsMatchers.promotionsList;
import static com.autonomy.abc.matchers.PromotionsMatchers.triggerList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private PromotionsPage promotionsPage;
	private SearchPage searchPage;
	private SearchActionFactory searchActionFactory;
	private PromotionActionFactory promotionActionFactory;


	@Before
	public void setUp() throws MalformedURLException {
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		// TODO: occasional stale element?
		searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
		promotionActionFactory = new PromotionActionFactory(getApplication(), getElementFactory());
		promotionActionFactory.makeDeleteAll().apply();
	}

	private List<String> setUpPromotion(Search search, int numberOfDocs, Promotion promotion) {
		List<String> promotedDocTitles = new ArrayList<>();
		promotionActionFactory.makeCreatePromotion(promotion, search, numberOfDocs).apply();
		// wait for search page to load before navigating away
		getElementFactory().getSearchPage();
		promotionsPage = promotion.getDetailsPage(body, getElementFactory());
		return promotedDocTitles;
	}

	private List<String> setUpPromotion(Search search, Promotion promotion) {
		return setUpPromotion(search, 1, promotion);
	}

	private List<String> setUpCarsPromotion(int numberOfDocs) {
//		final List<String> promotedDocTitles = promotionsPage.setUpANewMultiDocPromotion("English", "cars", "Sponsored", "wheels", 2, getConfig().getType().getName());
		return setUpPromotion(searchActionFactory.makeSearch("cars"), numberOfDocs, new SpotlightPromotion("wheels"));
	}

	private Search search(String searchTerm, String language) {
		return searchActionFactory.makeSearch(searchTerm).applyFilter(new LanguageFilter(language));
	}


	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.promoteExistingButton().click();
		verifyThat("correct URL", getDriver().getCurrentUrl().endsWith("promotions/new"));
		verifyThat("correct title", getApplication().createAppBody(getDriver()).getTopNavBar(), containsText("Create New Promotion"));
	}

	// TODO: should work after CCUK-3394
	@Test
	public void testCorrectDocumentsInPromotion() {
		List<String> promotedDocTitles = setUpCarsPromotion(2);
		List<String> promotedList = promotionsPage.getPromotedList();
		verifyThat(promotedDocTitles, everyItem(isIn(promotedList)));
	}

	@Test
	public void testDeletePromotedDocuments() {
		List<String> promotedDocTitles = setUpCarsPromotion(4);
		int numberOfDocuments = promotionsPage.getPromotedList().size();
		verifyThat(numberOfDocuments, is(4));

		for (final String title : promotedDocTitles) {
			promotionsPage.deleteDocument(title);
			numberOfDocuments--;

			if (numberOfDocuments == 1) {
				assertThat(promotionsPage.getPromotedList(), hasSize(1));
				verifyThat("remove document button is not visible when a single document", promotionsPage, not(containsElement(By.className("remove-document-reference"))));
				break;
			}
		}
	}

	@Test
	public void testWhitespaceTrigger() {
		setUpCarsPromotion(1);

		promotionsPage.tryClickThenTryParentClick(promotionsPage.triggerAddButton());

		verifyThat(promotionsPage, triggerList(hasSize(1)));

		promotionsPage.addSearchTrigger("trigger");
		verifyThat("added valid trigger", promotionsPage, triggerList(hasSize(2)));

		String[] invalidTriggers = {"   ", " trigger", "\t"};
		for (String trigger : invalidTriggers) {
			promotionsPage.addSearchTrigger(trigger);
			verifyThat("'" + trigger + "' is not accepted as a valid trigger", promotionsPage, triggerList(hasSize(2)));
		}
	}

	@Test
	public void testQuotesTrigger() throws InterruptedException {
		setUpCarsPromotion(1);

		verifyThat(promotionsPage, triggerList(hasSize(1)));

		promotionsPage.addSearchTrigger("bag");
		verifyThat("added valid trigger", promotionsPage, triggerList(hasSize(2)));

		String[] invalidTriggers = {"\"bag", "bag\"", "\"bag\""};
		for (String trigger : invalidTriggers) {
			promotionsPage.addSearchTrigger(trigger);
			verifyThat("'" + trigger + "' is not accepted as a valid trigger", promotionsPage, triggerList(hasSize(2)));
		}
	}

	@Test
	public void testCommasTrigger() {
		setUpCarsPromotion(1);
		verifyThat(promotionsPage, triggerList(hasSize(1)));

		promotionsPage.addSearchTrigger("France");
		verifyThat(promotionsPage, triggerList(hasSize(2)));

		String[] invalidTriggers = {",Germany", "Ita,ly Spain", "Ireland, Belgium", "UK , Luxembourg"};
		for (String trigger : invalidTriggers) {
			promotionsPage.addSearchTrigger(trigger);
			verifyThat("'" + trigger + "' does not add a new trigger", promotionsPage, triggerList(hasSize(2)));
			verifyThat("'" + trigger + "' produces an error message", promotionsPage, containsText("Terms may not contain commas. Separate words and phrases with whitespace."));
		}

		promotionsPage.addSearchTrigger("Greece Romania");
		assertThat(promotionsPage, triggerList(hasSize(4)));
		assertThat("error message no longer showing", promotionsPage, not(containsText("Terms may not contain commas. Separate words and phrases with whitespace.")));
	}

	@Test
	public void testHTMLTrigger() {
		setUpCarsPromotion(1);
		final String trigger = "<h1>Hi</h1>";
		promotionsPage.addSearchTrigger(trigger);

		assertThat("triggers are HTML escaped", promotionsPage, triggerList(hasItem(trigger)));
	}

	// fails on-prem due to CCUK-2671
	@Test
	public void testAddRemoveTriggers() throws InterruptedException {
		setUpCarsPromotion(1);

		promotionsPage.addSearchTrigger("alpha");
		promotionsPage.waitForTriggerUpdate();
		promotionsPage.removeSearchTrigger("wheels");
		promotionsPage.waitForTriggerUpdate();
		verifyThat(promotionsPage, triggerList(hasSize(1)));

		verifyThat(promotionsPage, triggerList(not(hasItem("wheels"))));

		promotionsPage.addSearchTrigger("beta gamma delta");
		promotionsPage.waitForTriggerUpdate();
		promotionsPage.removeSearchTrigger("gamma");
		promotionsPage.removeSearchTrigger("alpha");
		promotionsPage.addSearchTrigger("epsilon");
		promotionsPage.removeSearchTrigger("beta");
		promotionsPage.waitForTriggerUpdate();

		verifyThat(promotionsPage, triggerList(hasSize(2)));
		verifyThat(promotionsPage, triggerList(not(hasItem("beta"))));
		verifyThat(promotionsPage, triggerList(hasItem("epsilon")));

		promotionsPage.removeSearchTrigger("epsilon");
		promotionsPage.waitForTriggerUpdate();
		verifyThat(promotionsPage, not(containsElement(By.className("remove-word"))));
	}

	@Test
	public void testBackButton() {
		setUpCarsPromotion(1);
		promotionsPage.backButton().click();
		assertThat("Back button redirects to main promotions page", getDriver().getCurrentUrl().endsWith("promotions"));
	}

	@Test
	public void testEditPromotionName() throws InterruptedException {
		setUpCarsPromotion(1);
		verifyThat(promotionsPage.getPromotionTitle(), (is("Spotlight for: wheels")));

		String[] newTitles = {"Fuzz", "<script> alert(\"hi\") </script>"};
		for (String newTitle : newTitles) {
			promotionsPage.createNewTitle(newTitle);
			verifyThat(promotionsPage.getPromotionTitle(), (is(newTitle)));
		}
	}

	@Test
	public void testEditPromotionType() {
		// cannot edit promotion type for hosted
		assumeThat(config.getType(), equalTo(ApplicationType.ON_PREM));
		setUpCarsPromotion(1);
		verifyThat(promotionsPage.getPromotionType(), is("Sponsored"));

		promotionsPage.changeSpotlightType("Hotwire");
		verifyThat(promotionsPage.getPromotionType(), is("Hotwire"));

		promotionsPage.changeSpotlightType("Top Promotions");
		verifyThat(promotionsPage.getPromotionType(), is("Top Promotions"));

		promotionsPage.changeSpotlightType("Sponsored");
		verifyThat(promotionsPage.getPromotionType(), is("Sponsored"));
	}

	@Test
	public void testDeletePromotions() throws InterruptedException {
		String[] searchTerms = {"rabbit", "horse", "script"};
		String[] triggers = {"bunny", "pony", "<script> document.body.innerHTML = '' </script>"};
		for (int i=0; i<searchTerms.length; i++) {
			setUpPromotion(searchActionFactory.makeSearch(searchTerms[i]), new SpotlightPromotion(triggers[i]));
			promotionsPage.backButton().click();
		}

		// "script" gets mangled
		String[] searchableTriggers = {"bunny", "pony", "script"};
		for (String trigger : searchableTriggers) {
			verifyThat(promotionsPage, promotionsList(hasItem(containsText(trigger))));
		}
		verifyThat(promotionsPage, promotionsList(hasSize(3)));

		promotionsPage.getPromotionLinkWithTitleContaining("bunny").click();
		promotionsPage.deletePromotion();

		verifyThat("promotion 'pony' still exists", promotionsPage, promotionsList(hasItem(containsText("pony"))));
		verifyThat("promotion 'script' still exists", promotionsPage, promotionsList(hasItem(containsText("script"))));
		verifyThat("deleted promotion 'bunny'", promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.getPromotionLinkWithTitleContaining("script").click();
		promotionsPage.deletePromotion();

		verifyThat("promotion 'pony' still exists", promotionsPage, promotionsList(hasItem(containsText("pony"))));
		verifyThat("deleted promotion 'bunny'", promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.getPromotionLinkWithTitleContaining("pony").click();
		promotionsPage.deletePromotion();

		verifyThat("deleted promotion 'pony'", promotionsPage, promotionsList(hasSize(0)));
	}

	@Ignore
	@Test
	public void testAddingLotsOfDocsToAPromotion() {
		setUpPromotion(searchActionFactory.makeSearch("sith"), 100, new SpotlightPromotion("darth sith"));
		assertThat(promotionsPage, promotionsList(hasSize(100)));
	}

	@Test
	public void testPromotionFilter() throws InterruptedException {
//		assumeThat(config.getType(), equalTo(ApplicationType.ON_PREM));

		Search[] searches = {
				search("chien", "French"),
				search("الكلب", "Arabic"),
				search("dog", "English"),
				search("mbwa", "Swahili"),
				search("mbwa", "Swahili"),
				search("hond", "Afrikaans"),
				search("hond", "Afrikaans"),
		};
		Promotion[] promotions = {
				new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "woof bark"),
				new SpotlightPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "dog chien"),
				new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "hound pooch"),
				new PinToPositionPromotion(3, "woof swahili"),
				new PinToPositionPromotion(3, "pooch swahili"),
				new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, "pooch hond wolf"),
				new DynamicPromotion(5, "lupo wolf")
		};

		for (int i = 0; i < searches.length; i++) {
			setUpPromotion(searches[i], promotions[i]);
			promotionsPage.backButton().click();
		}
		assertThat(promotionsPage, promotionsList(hasSize(searches.length)));

		List<String> promotionTitles = promotionsPage.getPromotionTitles();
		for (int i = 0; i < promotionTitles.size() - 1; i++) {
			verifyThat(promotionTitles.get(i).toLowerCase(), lessThanOrEqualTo(promotionTitles.get(i + 1).toLowerCase()));
		}

		promotionsPage.getPromotionLinkWithTitleContaining(promotionTitles.get(3)).click();
		promotionsPage.createNewTitle("aaa");
		promotionsPage.loadOrFadeWait();
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();

		final List<String> promotionsAgain = promotionsPage.getPromotionTitles();
		for (int i = 0; i < promotionsAgain.size() - 1; i++) {
			verifyThat(promotionsAgain.get(i).toLowerCase(), lessThanOrEqualTo(promotionsAgain.get(i + 1).toLowerCase()));
		}

		promotionsPage.getPromotionLinkWithTitleContaining(promotionTitles.get(3)).click();
		promotionsPage.createNewTitle(promotionTitles.get(3));
		promotionsPage.loadOrFadeWait();
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();

		promotionsPage.promotionsSearchFilter().sendKeys("dog");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		verifyThat(promotionsPage, promotionsList(hasSize(3)));
		promotionTitles = promotionsPage.getPromotionTitles();
		for (int i = 0; i < promotionTitles.size() - 1; i++) {
			verifyThat(promotionTitles.get(i).toLowerCase(), lessThanOrEqualTo(promotionTitles.get(i + 1).toLowerCase()));
		}

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.createNewTitle("hound");
		verifyThat(promotionsPage.getPromotionTitle(), is("hound"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		verifyThat(promotionsPage, promotionsList(hasSize(3)));

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.removeSearchTrigger("pooch");
		promotionsPage.waitForTriggerUpdate();
		verifyThat(promotionsPage, triggerList(not(hasItem("pooch"))));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		verifyThat(promotionsPage.promotionsCategoryFilterValue(), is("All Types"));

		promotionsPage.selectPromotionsCategoryFilter("Spotlight");
		promotionsPage.clearPromotionsSearchFilter();
		verifyThat(promotionsPage.promotionsCategoryFilterValue(), is("Spotlight"));
		verifyThat(promotionsPage, promotionsList(hasSize(3)));

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.selectPromotionsCategoryFilter("Pin to Position");
		promotionsPage.clearPromotionsSearchFilter();
		verifyThat(promotionsPage.promotionsCategoryFilterValue(), is("Pin to Position"));
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.clearPromotionsSearchFilter();
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.getPromotionLinkWithTitleContaining("lupo").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.removeSearchTrigger("wolf");
		promotionsPage.waitForTriggerUpdate();
		verifyThat(promotionsPage, triggerList(not(hasItem("wolf"))));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.getPromotionLinkWithTitleContaining("lupo").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.createNewTitle("lupo");
		verifyThat(promotionsPage.getPromotionTitle(), is("lupo"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.getPromotionLinkWithTitleContaining("hond").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.addSearchTrigger("Rhodesian Ridgeback");
		promotionsPage.waitForTriggerUpdate();
		verifyThat(promotionsPage, triggerList(hasItems("Rhodesian", "Ridgeback")));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("Rhodesian");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.selectPromotionsCategoryFilter("All Types");
		promotionsPage.clearPromotionsSearchFilter();
		// OP fails due to CCUK-2671
		promotionsPage.promotionsSearchFilter().sendKeys("Ridgeback");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));
	}

	@Test
	public void testPromotionLanguages() {
		// TODO: IOD-4857
		assumeThat(config.getType(), equalTo(ApplicationType.ON_PREM));
		String[] languages = {"French", "Swahili", "Afrikaans"};
		String[] searchTerms = {"chien", "mbwa", "pooch"};
		Promotion[] promotions = {
				new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "woof bark"),
				new PinToPositionPromotion(3, "swahili woof"),
				new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, "hond wolf")
		};

		for (int i=0; i<languages.length; i++) {
			setUpPromotion(search(searchTerms[i], languages[i]), promotions[i]);
			verifyThat(promotionsPage.getLanguage(), is(languages[i]));
		}
	}

	@Test
	public void testEditDynamicQuery() throws InterruptedException {
		search("kitty", "French").apply();
		SearchPage searchPage = getElementFactory().getSearchPage();
		final String firstSearchResult = searchPage.getSearchResult(1).getText();
		final String secondSearchResult = setUpPromotion(search("chat", "French"), new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "meow")).get(0);

		promotionsPage.addSearchTrigger("purrr");
		promotionsPage.waitForTriggerUpdate();
		promotionsPage.removeSearchTrigger("meow");
		promotionsPage.waitForTriggerUpdate();
		search("purrr", "French").apply();
		verifyThat(searchPage.promotionsSummaryList(false).get(0), is(secondSearchResult));

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.selectPromotionsCategoryFilter("All Types");
		promotionsPage.loadOrFadeWait();
		promotionsPage.getPromotionLinkWithTitleContaining("meow").click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		verifyThat(promotionsPage.getQueryText(), is("chat"));

		promotionsPage.editQueryText("kitty");
		verifyThat(promotionsPage.getQueryText(), is("kitty"));

		search("purrr", "French").apply();
		verifyThat(searchPage.promotionsSummaryList(false).get(0), is(firstSearchResult));

		getDriver().navigate().refresh();
		searchPage = getElementFactory().getSearchPage();
		verifyThat(searchPage.promotionsSummaryList(false).get(0), is(firstSearchResult));
	}

	@Test
	public void testPromotionCreationAndDeletionOnSecondWindow() {
		setUpPromotion(search("chien", "French"), new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "woof bark"));

		promotionsPage.backButton().click();
		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = promotionsPage.createAndListWindowHandles();

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final PromotionsPage secondPromotionsPage = getElementFactory().getPromotionsPage();
		assertThat("Navigated to promotions menu", secondPromotionsPage.promoteExistingButton().isDisplayed());

		getDriver().switchTo().window(browserHandles.get(0));
		setUpPromotion(search("rafiki", "Swahili"), new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "friend"));

		getDriver().switchTo().window(browserHandles.get(1));
		verifyThat(secondPromotionsPage, promotionsList(hasSize(2)));

		getDriver().switchTo().window(browserHandles.get(0));
		promotionsPage.deletePromotion();

		getDriver().switchTo().window(browserHandles.get(1));
		verifyThat(secondPromotionsPage, promotionsList(hasSize(1)));

		secondPromotionsPage.getPromotionLinkWithTitleContaining("woof").click();
		secondPromotionsPage.deletePromotion();

		getDriver().switchTo().window(browserHandles.get(0));
		verifyThat(promotionsPage, containsText("There are no promotions..."));
	}

	@Test
	public void testCountSearchResultsWithPinToPositionInjected() {
		setUpPromotion(search("Lyon", "French"), new PinToPositionPromotion(13, "boeuf frites orange"));

		String[] queries = {"boeuf", "frites", "orange"};
		SearchPage searchPage;
		for (final String query : queries) {
			search(query, "French").apply();
			searchPage = getElementFactory().getSearchPage();
			final int firstPageStated = searchPage.countSearchResults();
			searchPage.forwardToLastPageButton().click();
			searchPage.waitForSearchLoadIndicatorToDisappear();
			final int numberOfPages = searchPage.getCurrentPageNumber();
			final int lastPageDocumentsCount = searchPage.visibleDocumentsCount();
			final int listedCount = (numberOfPages - 1) * searchPage.RESULTS_PER_PAGE + lastPageDocumentsCount;
			final int lastPageStated = searchPage.countSearchResults();
			verifyThat("count is the same across pages for " + query, firstPageStated, is(lastPageStated));
			verifyThat("count is correct for " + query, lastPageStated, is(listedCount));
		}
	}
}