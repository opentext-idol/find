package com.autonomy.abc.promotions;

import com.autonomy.abc.Trigger.SharedTriggerTests;
import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.framework.categories.SlowTest;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.Pagination;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsElement;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.PromotionsMatchers.promotionsList;
import static com.autonomy.abc.matchers.PromotionsMatchers.triggerList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class PromotionsITCase extends ABCTestBase {

	public PromotionsITCase(final TestConfig config) {
		super(config);
	}

	private PromotionsPage promotionsPage;
	private PromotionsDetailPage promotionsDetailPage;
	private SearchService searchService;
	private PromotionService<?> promotionService;

	@Before
	public void setUp() throws MalformedURLException {
		searchService = getApplication().searchService();
		promotionService = getApplication().promotionService();

		promotionsPage = promotionService.deleteAll();
	}

	private List<String> setUpPromotion(SearchQuery search, int numberOfDocs, Promotion promotion) {
		List<String> promotedDocTitles = promotionService.setUpPromotion(promotion, search, numberOfDocs);
		// wait for search page to load before navigating away
		getElementFactory().getSearchPage();
		promotionsDetailPage = promotionService.goToDetails(promotion);
		return promotedDocTitles;
	}

	private List<String> setUpPromotion(SearchQuery search, Promotion promotion) {
		return setUpPromotion(search, 1, promotion);
	}

	private List<String> setUpCarsPromotion(int numberOfDocs) {
		return setUpPromotion(new SearchQuery("cars"), numberOfDocs, new SpotlightPromotion("wheels"));
	}

	private SearchQuery getQuery(String searchTerm, Language language) {
		return new SearchQuery(searchTerm).withFilter(new LanguageFilter(language));
	}

	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.promoteExistingButton().click();
		verifyThat("correct URL", getDriver().getCurrentUrl().endsWith("promotions/new"));
		verifyThat("correct title", getElementFactory().getTopNavBar(), containsText("Create New Promotion"));
	}

	@Test
	@KnownBug("CCUK-3394")
	public void testCorrectDocumentsInPromotion() {
		List<String> promotedDocTitles = setUpCarsPromotion(16);
		List<String> promotedList = promotionsDetailPage.getPromotedTitles();
		verifyThat(promotedDocTitles, everyItem(isIn(promotedList)));
	}

	@Test
	public void testDeletePromotedDocuments() {
		List<String> promotedDocTitles = setUpCarsPromotion(4);
		int numberOfDocuments = promotionsDetailPage.getPromotedTitles().size();
		verifyThat(numberOfDocuments, is(4));

		for (final String title : promotedDocTitles) {
			promotionsDetailPage.removablePromotedDocument(title).removeAndWait();
			numberOfDocuments--;

			if (numberOfDocuments == 1) {
				assertThat(promotionsDetailPage.getPromotedTitles(), hasSize(1));
				verifyThat("remove document button is not visible when a single document", promotionsPage, not(containsElement(By.className("remove-document-reference"))));
				break;
			}
		}
	}

	@Test
	public void testTriggers(){
		setUpCarsPromotion(1);
		SharedTriggerTests.badTriggersTest(promotionsDetailPage.getTriggerForm());
	}

	@Test
	@KnownBug("CCUK-2671")
	public void testAddRemoveTriggers() throws InterruptedException {
		setUpCarsPromotion(1);

		SharedTriggerTests.addRemoveTriggers(promotionsDetailPage.getTriggerForm());
	}

	@Test
	public void testEditPromotionName() throws InterruptedException {
		setUpCarsPromotion(1);
		Editable title = promotionsDetailPage.promotionTitle();
		verifyThat(title.getValue(), (is("Spotlight for: wheels")));

		String[] newTitles = {"Fuzz", "<script> alert(\"hi\") </script>"};
		for (String newTitle : newTitles) {
			title.setValueAndWait(newTitle);
			verifyThat(title.getValue(), (is(newTitle)));
		}
	}

	@Test
	public void testEditPromotionType() {
		// cannot edit promotion type for hosted
		assumeThat(config.getType(), equalTo(ApplicationType.ON_PREM));
		setUpCarsPromotion(1);
		verifyThat(promotionsDetailPage.getPromotionType(), is("Sponsored"));

		Dropdown dropdown = promotionsDetailPage.spotlightTypeDropdown();
		dropdown.select("Hotwire");
		Waits.loadOrFadeWait();
		verifyThat(dropdown.getValue(), is("Hotwire"));

		dropdown.select("Top Promotions");
		Waits.loadOrFadeWait();
		verifyThat(dropdown.getValue(), is("Top Promotions"));

		dropdown.select("Sponsored");
		Waits.loadOrFadeWait();
		verifyThat(dropdown.getValue(), is("Sponsored"));
	}

	@Test
	public void testDeletePromotions() throws InterruptedException {
		String[] searchTerms = {"rabbit", "horse", "script"};
		String[] triggers = {"bunny", "pony", "<script> document.body.innerHTML = '' </script>"};
		for (int i=0; i<searchTerms.length; i++) {
			setUpPromotion(new SearchQuery(searchTerms[i]), new SpotlightPromotion(triggers[i]));
			promotionsPage = promotionService.goToPromotions();
		}

		// "script" gets mangled
		String[] searchableTriggers = {"bunny", "pony", "script"};
		for (String trigger : searchableTriggers) {
			verifyThat(promotionsPage, promotionsList(hasItem(containsText(trigger))));
		}
		verifyThat(promotionsPage, promotionsList(hasSize(3)));

		promotionService.delete("bunny");

		verifyThat("promotion 'pony' still exists", promotionsPage, promotionsList(hasItem(containsText("pony"))));
		verifyThat("promotion 'script' still exists", promotionsPage, promotionsList(hasItem(containsText("script"))));
		verifyThat("deleted promotion 'bunny'", promotionsPage, promotionsList(hasSize(2)));

		promotionService.delete("script");

		verifyThat("promotion 'pony' still exists", promotionsPage, promotionsList(hasItem(containsText("pony"))));
		verifyThat("deleted promotion 'script'", promotionsPage, promotionsList(hasSize(1)));

		promotionService.delete("pony");

		verifyThat("deleted promotion 'pony'", promotionsPage, promotionsList(hasSize(0)));
	}

	@Test
	@Category(SlowTest.class)
	@KnownBug("CSA-2022")
	public void testAddingLotsOfDocsToAPromotion() {
		int size = 100;
		boolean setUp = false;
		try {
			setUpPromotion(new SearchQuery("dog"), size, new SpotlightPromotion("golden retriever"));
			setUp = true;
		} catch (TimeoutException e) {
			/* failed to set up promotion */
		}
		assertThat("added promotion successfully", setUp);
		assertThat(promotionsDetailPage.getPromotedTitles(), hasSize(size));
	}

	private void renamePromotionContaining(String oldTitle, String newTitle) {
		promotionsDetailPage = promotionService.goToDetails(oldTitle);
		promotionsDetailPage.promotionTitle().setValueAndWait(newTitle);
		promotionsPage = promotionService.goToPromotions();
	}

	@Test
	@KnownBug("CCUK-2671")
	public void testPromotionFilter() throws InterruptedException {
		// hosted does not have foreign content indexed
		SearchQuery[] searches;
		if (config.getType().equals(ApplicationType.ON_PREM)) {
			searches = new SearchQuery[]{
					getQuery("chien", Language.FRENCH),
					getQuery("الكلب", Language.ARABIC),
					getQuery("dog", Language.ENGLISH),
					getQuery("mbwa", Language.SWAHILI),
					getQuery("mbwa", Language.SWAHILI),
					getQuery("hond", Language.AFRIKAANS),
					getQuery("hond", Language.AFRIKAANS)
			};
		} else {
			searches = new SearchQuery[]{
					getQuery("marge", Language.ENGLISH),
					getQuery("homer", Language.ENGLISH),
					getQuery("dog", Language.ENGLISH),
					getQuery("bart", Language.ENGLISH),
					getQuery("bart", Language.ENGLISH),
					getQuery("lisa", Language.ENGLISH),
					getQuery("lisa", Language.ENGLISH)
			};
		}
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
			promotionService.goToPromotions();
		}
		assertThat(promotionsPage, promotionsList(hasSize(searches.length)));

		List<String> promotionTitles = promotionsPage.getPromotionTitles();
		for (int i = 0; i < promotionTitles.size() - 1; i++) {
			verifyThat(promotionTitles.get(i).toLowerCase(), lessThanOrEqualTo(promotionTitles.get(i + 1).toLowerCase()));
		}

		renamePromotionContaining(promotionTitles.get(3), "aaa");

		final List<String> promotionsAgain = promotionsPage.getPromotionTitles();
		for (int i = 0; i < promotionsAgain.size() - 1; i++) {
			verifyThat(promotionsAgain.get(i).toLowerCase(), lessThanOrEqualTo(promotionsAgain.get(i + 1).toLowerCase()));
		}

		renamePromotionContaining(promotionTitles.get(3), promotionTitles.get(3));

		int results = 1;
		for(String search : Arrays.asList("dog", "wolf", "pooch")){
			promotionsPage.clearPromotionsSearchFilter();
			promotionsPage.promotionsSearchFilter().sendKeys(search);
			verifyThat(promotionsPage, promotionsList(hasSize(results)));
			results++;
		}

		promotionTitles = promotionsPage.getPromotionTitles();
		for (int i = 0; i < promotionTitles.size() - 1; i++) {
			verifyThat(promotionTitles.get(i).toLowerCase(), lessThanOrEqualTo(promotionTitles.get(i + 1).toLowerCase()));
		}

		renamePromotionContaining("hound", "hound");

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		verifyThat(promotionsPage, promotionsList(hasSize(3)));

		promotionsDetailPage = promotionService.goToDetails("pooch");
		promotionsDetailPage.getTriggerForm().removeTrigger("pooch");
		verifyThat(promotionsDetailPage, triggerList(not(hasItem("pooch"))));
		promotionService.goToPromotions();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		verifyThat(promotionsPage, promotionsList(hasSize(3)));

		verifyThat(promotionsPage.promotionsCategoryFilterValue(), equalToIgnoringCase("All Types"));

		promotionsPage.selectPromotionsCategoryFilter("Spotlight");
		promotionsPage.clearPromotionsSearchFilter();
		verifyThat(promotionsPage.promotionsCategoryFilterValue(), equalToIgnoringCase("Spotlight"));
		verifyThat(promotionsPage, promotionsList(hasSize(3)));

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.selectPromotionsCategoryFilter("Pin to Position");
		promotionsPage.clearPromotionsSearchFilter();
		verifyThat(promotionsPage.promotionsCategoryFilterValue(), equalToIgnoringCase("Pin to Position"));
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.clearPromotionsSearchFilter();
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		promotionsDetailPage = promotionService.goToDetails("lupo");
		promotionsDetailPage.getTriggerForm().removeTrigger("wolf");
		verifyThat(promotionsDetailPage, triggerList(not(hasItem("wolf"))));
		promotionService.goToPromotions();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		verifyThat(promotionsPage, promotionsList(hasSize(2)));

		renamePromotionContaining("lupo", "lupo");

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsDetailPage = promotionService.goToDetails("hond");
		promotionsDetailPage.getTriggerForm().addTrigger("Rhodesian Ridgeback");
		verifyThat(promotionsDetailPage, triggerList(hasItems("rhodesian", "ridgeback")));
		promotionService.goToPromotions();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("Rhodesian");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));

		promotionsPage.selectPromotionsCategoryFilter("All Types");
		promotionsPage.clearPromotionsSearchFilter();
		// OP fails due to rapid add/remove bug
		promotionsPage.promotionsSearchFilter().sendKeys("Ridgeback");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));
	}

	@Test
	@RelatedTo("IOD-4827")
	public void testPromotionLanguages() {
		assumeThat(config.getType(), equalTo(ApplicationType.ON_PREM));
		Language[] languages = {Language.FRENCH, Language.SWAHILI, Language.AFRIKAANS};
		//Afrikaans dog thing isn't actually a dog but it wasn't working so yolo
		String[] searchTerms = {"chien", "mbwa", "bergaalwyn"};
		Promotion[] promotions = {
				new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "woof bark"),
				new PinToPositionPromotion(3, "swahili woof"),
				new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, "hond wolf")
		};

		for (int i=0; i<languages.length; i++) {
			setUpPromotion(getQuery(searchTerms[i], languages[i]), promotions[i]);
			verifyThat(promotionsDetailPage.getLanguage(), is(languages[i].toString()));
		}
	}

	@Test
	public void testPromotionCreationAndDeletionOnSecondWindow() {
		setUpPromotion(getQuery("chien", Language.FRENCH), new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "woof bark"));

		promotionService.goToPromotions();
		final String url = getDriver().getCurrentUrl();
		final Window mainWindow = getMainSession().getActiveWindow();
		final Window secondWindow = getMainSession().openWindow(url);

		secondWindow.activate();
		getDriver().get(url);
		final PromotionsPage secondPromotionsPage = getElementFactory().getPromotionsPage();
		assertThat("Navigated to promotions menu", secondPromotionsPage.promoteExistingButton().isDisplayed());

		mainWindow.activate();
		setUpPromotion(getQuery("nein", Language.GERMAN), new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "friend"));

		secondWindow.activate();
		verifyThat(secondPromotionsPage, promotionsList(hasSize(2)));

		mainWindow.activate();
		promotionService.goToPromotions();
		promotionService.delete("friend");

		secondWindow.activate();
		verifyThat(secondPromotionsPage, promotionsList(hasSize(1)));
		promotionService.delete("woof");

		mainWindow.activate();
		verifyThat(promotionsPage, containsText("There are no promotions..."));
	}

	@Test
	public void testCountSearchResultsWithPinToPositionInjected() {
		setUpPromotion(getQuery("donut", Language.ENGLISH), new PinToPositionPromotion(13, "round tasty snack"));

		String[] queries = {"round", "tasty", "snack"};
		SearchPage searchPage;
		for (final String query : queries) {
			searchService.search(getQuery(query, Language.ENGLISH));
			searchPage = getElementFactory().getSearchPage();
			final int firstPageStated = searchPage.getHeadingResultsCount();
			searchPage.switchResultsPage(Pagination.LAST);
			final int numberOfPages = searchPage.getCurrentPageNumber();
			final int lastPageDocumentsCount = searchPage.visibleDocumentsCount();
			final int listedCount = (numberOfPages - 1) * SearchPage.RESULTS_PER_PAGE + lastPageDocumentsCount;
			final int lastPageStated = searchPage.getHeadingResultsCount();
			verifyThat("count is the same across pages for " + query, firstPageStated, is(lastPageStated));
			verifyThat("count is correct for " + query, lastPageStated, is(listedCount));
		}
	}

	// fails on Chrome - seems to be an issue with ChromeDriver
	@Test
	public void testSpotlightViewable() {
		List<String> promotedDocs = setUpCarsPromotion(3);
		SearchPage searchPage = searchService.search("wheels");
		final String handle = getDriver().getWindowHandle();

		WebElement promotedResult = searchPage.promotedDocumentTitle(1);
		String firstTitle = promotedResult.getText();
		String secondTitle = searchPage.promotedDocumentTitle(2).getText();
		verifyThat(firstTitle, isIn(promotedDocs));
		promotedResult.click();
		DocumentViewer documentViewer = DocumentViewer.make(getDriver());
		verifyFrame(documentViewer.getReference(), documentViewer.frame());

		getDriver().switchTo().window(handle);
		documentViewer.next();

		verifyThat(secondTitle, isIn(promotedDocs));
		verifyFrame(documentViewer.getReference(), documentViewer.frame());

		getDriver().switchTo().window(handle);
		documentViewer.previous();
		getDriver().switchTo().frame(documentViewer.frame());
		verifyThat("first document loads again", getDriver().findElement(By.xpath(".//body")).getText(), not(isEmptyOrNullString()));

		getDriver().switchTo().window(handle);
		documentViewer.close();

		searchPage.showMorePromotions();
		promotedResult = searchPage.promotedDocumentTitle(3);
		String thirdTitle = promotedResult.getText();
		verifyThat(thirdTitle, isIn(promotedDocs));

		promotedResult.click();
		documentViewer = DocumentViewer.make(getDriver());
		verifyFrame(documentViewer.getReference(), documentViewer.frame());
		getDriver().switchTo().window(handle);
	}

	private void verifyFrame(String reference, WebElement frame) {
		verifyThat("Document has a reference", reference, not(isEmptyOrNullString()));
		getDriver().switchTo().frame(frame);
		verifyThat("Document loads", getDriver().findElement(By.xpath(".//body")).getText(), not(isEmptyOrNullString()));
	}

	@Test
	@KnownBug("CCUK-3457")
	public void testPromotingItemsWithBrackets(){
		SpotlightPromotion spotlightPromotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "imagine dragons");
		SearchQuery query = new SearchQuery("\"Selenium (software)\"").withFilter(new IndexFilter("wiki_eng"));

		SearchPage searchPage = searchService.search(query);
		assumeThat("Was expecting Selenium (Software) to be the first result", searchPage.getSearchResult(1).getTitleString(), is("Selenium (software)"));

		promotionService.setUpPromotion(spotlightPromotion, query, 1);
		PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails(spotlightPromotion);

		List<String> promotedDocuments = promotionsDetailPage.getPromotedTitles();

		verifyThat(promotedDocuments.size(), is(1));
		verifyThat(promotedDocuments.get(0), is("Selenium (software)"));
	}
}
