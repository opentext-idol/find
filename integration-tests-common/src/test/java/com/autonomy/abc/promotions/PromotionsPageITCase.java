package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.*;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import com.autonomy.abc.selenium.util.DriverUtil;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Waits;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
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

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private PromotionsPage promotionsPage;
	private PromotionsDetailPage promotionsDetailPage;
	private SearchActionFactory searchActionFactory;
	private PromotionService promotionService;

	@Before
	public void setUp() throws MalformedURLException {
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		// TODO: occasional stale element?
		searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
		promotionService = getApplication().createPromotionService(getElementFactory());
		promotionService.deleteAll();
	}

	private List<String> setUpPromotion(Search search, int numberOfDocs, Promotion promotion) {
		List<String> promotedDocTitles = promotionService.setUpPromotion(promotion, search, numberOfDocs);
		// wait for search page to load before navigating away
		getElementFactory().getSearchPage();
		promotionsDetailPage = promotionService.goToDetails(promotion);
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
	public void testWhitespaceTrigger() {
		setUpCarsPromotion(1);

		TriggerForm triggerForm = promotionsDetailPage.getTriggerForm();
		try {
			triggerForm.addTrigger("");
		} catch (Exception e) {
			e.printStackTrace();
		}

		verifyThat(promotionsDetailPage, triggerList(hasSize(1)));

		triggerForm.addTrigger("trigger");
		verifyThat("added valid trigger", promotionsDetailPage, triggerList(hasSize(2)));

		String[] invalidTriggers = {"   ", " trigger", "\t"};
		for (String trigger : invalidTriggers) {
			triggerForm.addTrigger(trigger);
			verifyThat("'" + trigger + "' is not accepted as a valid trigger", promotionsDetailPage, triggerList(hasSize(2)));
		}
	}

	@Test
	public void testQuotesTrigger() throws InterruptedException {
		setUpCarsPromotion(1);

		verifyThat(promotionsDetailPage, triggerList(hasSize(1)));

		TriggerForm triggerForm = promotionsDetailPage.getTriggerForm();

		triggerForm.addTrigger("bag");
		verifyThat("added valid trigger", promotionsDetailPage, triggerList(hasSize(2)));

		String[] invalidTriggers = {"\"bag", "bag\"", "\"bag\""};
		for (String trigger : invalidTriggers) {
			triggerForm.addTrigger(trigger);
			verifyThat("'" + trigger + "' is not accepted as a valid trigger", promotionsDetailPage, triggerList(hasSize(2)));
		}
	}

	@Test
	public void testCommasTrigger() {
		setUpCarsPromotion(1);
		verifyThat(promotionsDetailPage, triggerList(hasSize(1)));

		TriggerForm triggerForm = promotionsDetailPage.getTriggerForm();

		triggerForm.addTrigger("France");
		verifyThat(promotionsDetailPage, triggerList(hasSize(2)));

		String[] invalidTriggers = {",Germany", "Ita,ly Spain", "Ireland, Belgium", "UK , Luxembourg"};
		for (String trigger : invalidTriggers) {
			triggerForm.addTrigger(trigger);
			verifyThat("'" + trigger + "' does not add a new trigger", promotionsDetailPage, triggerList(hasSize(2)));
			verifyThat("'" + trigger + "' produces an error message", promotionsPage, containsText(Errors.Term.COMMAS));
		}

		triggerForm.addTrigger("Greece Romania");
		assertThat(promotionsDetailPage, triggerList(hasSize(4)));
		assertThat("error message no longer showing", promotionsPage, not(containsText(Errors.Term.COMMAS)));
	}

	@Test
	public void testHTMLTrigger() {
		setUpCarsPromotion(1);
		final String trigger = "<h1>hi</h1>";
		promotionsDetailPage.getTriggerForm().addTrigger(trigger);

		assertThat("triggers are HTML escaped", promotionsDetailPage, triggerList(hasItem(trigger)));
	}

	// fails on-prem due to CCUK-2671
	@Test
	public void testAddRemoveTriggers() throws InterruptedException {
		setUpCarsPromotion(1);

		PromotionsDetailTriggerForm triggerForm = promotionsDetailPage.getTriggerForm();

		triggerForm.addTrigger("alpha");
		triggerForm.removeTrigger("wheels");
		verifyThat(promotionsDetailPage, triggerList(hasSize(1)));

		verifyThat(promotionsDetailPage, triggerList(not(hasItem("wheels"))));

		triggerForm.addTrigger("beta gamma delta");
		triggerForm.removeTriggerAsync("gamma");
		triggerForm.removeTriggerAsync("alpha");
		//TODO should this be trying to add and remove a trigger at the same time?
		triggerForm.addTrigger("epsilon");
		triggerForm.removeTriggerAsync("beta");
		triggerForm.waitForTriggerRefresh();

		verifyThat(promotionsDetailPage, triggerList(hasSize(2)));
		verifyThat(promotionsDetailPage, triggerList(not(hasItem("beta"))));
		verifyThat(promotionsDetailPage, triggerList(hasItem("epsilon")));

		triggerForm.removeTrigger("epsilon");
		verifyThat(promotionsPage, not(containsElement(By.className("remove-word"))));
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
			setUpPromotion(searchActionFactory.makeSearch(searchTerms[i]), new SpotlightPromotion(triggers[i]));
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

	@Ignore
	@Test
	public void testAddingLotsOfDocsToAPromotion() {
		setUpPromotion(searchActionFactory.makeSearch("sith"), 100, new SpotlightPromotion("darth sith"));
		assertThat(promotionsPage, promotionsList(hasSize(100)));
	}

	private void renamePromotionContaining(String oldTitle, String newTitle) {
		promotionsDetailPage = promotionService.goToDetails(oldTitle);
		promotionsDetailPage.promotionTitle().setValueAndWait(newTitle);
		promotionsPage = promotionService.goToPromotions();
	}

	@Test
	public void testPromotionFilter() throws InterruptedException {
		// hosted does not have foreign content indexed
		Search[] searches;
		if (config.getType().equals(ApplicationType.ON_PREM)) {
			searches = new Search[]{
					search("chien", "French"),
					search("الكلب", "Arabic"),
					search("dog", "English"),
					search("mbwa", "Swahili"),
					search("mbwa", "Swahili"),
					search("hond", "Afrikaans"),
					search("hond", "Afrikaans")
			};
		} else {
			searches = new Search[]{
					search("marge", "English"),
					search("homer", "English"),
					search("dog", "English"),
					search("bart", "English"),
					search("bart", "English"),
					search("lisa", "English"),
					search("lisa", "English")
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
		// OP fails due to CCUK-2671
		promotionsPage.promotionsSearchFilter().sendKeys("Ridgeback");
		verifyThat(promotionsPage, promotionsList(hasSize(1)));
	}

	@Test
	public void testPromotionLanguages() {
		// TODO: IOD-4827
		assumeThat(config.getType(), equalTo(ApplicationType.ON_PREM));
		String[] languages = {"French", "Swahili", "Afrikaans"};
//		String[] searchTerms = {"chien", "mbwa", "pooch"};
		//Afrikaans dog thing isn't actually a dog but it wasn't working so yolo
		String[] searchTerms = {"chien", "mbwa", "bergaalwyn"};
		Promotion[] promotions = {
				new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "woof bark"),
				new PinToPositionPromotion(3, "swahili woof"),
				new DynamicPromotion(Promotion.SpotlightType.HOTWIRE, "hond wolf")
		};

		for (int i=0; i<languages.length; i++) {
			setUpPromotion(search(searchTerms[i], languages[i]), promotions[i]);
			verifyThat(promotionsDetailPage.getLanguage(), is(languages[i]));
		}
	}

	@Test
	public void testEditDynamicQuery() throws InterruptedException {
		search("kitty", "French").apply();
		SearchPage searchPage = getElementFactory().getSearchPage();
		final String firstSearchResult = searchPage.getSearchResult(1).getText();
		final String secondSearchResult = setUpPromotion(search("chat", "French"), new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "meow")).get(0);

		PromotionsDetailTriggerForm triggerForm = promotionsDetailPage.getTriggerForm();
		triggerForm.addTrigger("tigre");
		triggerForm.removeTrigger("meow");
		search("tigre", "French").apply();
		verifyThat(searchPage.getPromotedDocumentTitles(false).get(0), is(secondSearchResult));

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
//		promotionsPage.selectPromotionsCategoryFilter("All Types");
//		Waits.loadOrFadeWait();
		promotionsDetailPage = promotionService.goToDetails("meow");

		Editable queryText = promotionsDetailPage.queryText();
		verifyThat(queryText.getValue(), is("chat"));

		queryText.setValueAndWait("kitty");
		verifyThat(queryText.getValue(), is("kitty"));

		search("tigre", "French").apply();
		verifyThat(searchPage.getPromotedDocumentTitles(false).get(0), is(firstSearchResult));

		getDriver().navigate().refresh();
		searchPage = getElementFactory().getSearchPage();
		verifyThat(searchPage.getPromotedDocumentTitles(false).get(0), is(firstSearchResult));
	}

	@Test
	public void testPromotionCreationAndDeletionOnSecondWindow() {
		setUpPromotion(search("chien", "French"), new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "woof bark"));

		promotionService.goToPromotions();
		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = DriverUtil.createAndListWindowHandles(getDriver());

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final PromotionsPage secondPromotionsPage = getElementFactory().getPromotionsPage();
		assertThat("Navigated to promotions menu", secondPromotionsPage.promoteExistingButton().isDisplayed());

		getDriver().switchTo().window(browserHandles.get(0));
		setUpPromotion(search("nein", "German"), new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "friend"));

		getDriver().switchTo().window(browserHandles.get(1));
		verifyThat(secondPromotionsPage, promotionsList(hasSize(2)));

		getDriver().switchTo().window(browserHandles.get(0));
		promotionService.goToPromotions();
		promotionService.delete("friend");

		getDriver().switchTo().window(browserHandles.get(1));
		verifyThat(secondPromotionsPage, promotionsList(hasSize(1)));
//		promotionService.delete("woof");
		secondPromotionsPage.deletePromotion("woof");

		getDriver().switchTo().window(browserHandles.get(0));
		verifyThat(promotionsPage, containsText("There are no promotions..."));
	}

	@Test
	public void testCountSearchResultsWithPinToPositionInjected() {
		setUpPromotion(search("donut", "English"), new PinToPositionPromotion(13, "round tasty snack"));

		String[] queries = {"round", "tasty", "snack"};
		SearchPage searchPage;
		for (final String query : queries) {
			search(query, "English").apply();
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
		SearchPage searchPage = searchActionFactory.makeSearch("wheels").apply();
		final String handle = getDriver().getWindowHandle();

		WebElement promotedResult = searchPage.getPromotedResult(1);
		String firstTitle = promotedResult.getText();
		String secondTitle = searchPage.getPromotedResult(2).getText();
		verifyThat(firstTitle, isIn(promotedDocs));
		promotedResult.click();
		DocumentViewer documentViewer = DocumentViewer.make(getDriver());
		verifyThat("first document has a reference", documentViewer.getField("Reference"), not(isEmptyOrNullString()));
		getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
		verifyThat("first document loads", getDriver().findElement(By.xpath(".//body")).getText(), not(isEmptyOrNullString()));

		getDriver().switchTo().window(handle);
		documentViewer.next();

		verifyThat(secondTitle, isIn(promotedDocs));
		verifyThat("second document has a reference", documentViewer.getField("Reference"), not(isEmptyOrNullString()));
		getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
		verifyThat("second document loads", getDriver().findElement(By.xpath(".//body")).getText(), not(isEmptyOrNullString()));

		getDriver().switchTo().window(handle);
		documentViewer.previous();
		getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
		verifyThat("first document loads again", getDriver().findElement(By.xpath(".//body")).getText(), not(isEmptyOrNullString()));

		getDriver().switchTo().window(handle);
		documentViewer.close();

		searchPage.showMorePromotions();
		promotedResult = searchPage.getPromotedResult(3);
		String thirdTitle = promotedResult.getText();
		verifyThat(thirdTitle, isIn(promotedDocs));

		promotedResult.click();
		documentViewer = DocumentViewer.make(getDriver());
		verifyThat("third document has a reference", documentViewer.getField("Reference"), not(isEmptyOrNullString()));
		getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
		verifyThat("third document loads", getDriver().findElement(By.xpath(".//body")).getText(), not(isEmptyOrNullString()));
		getDriver().switchTo().window(handle);
	}

	@Test
	//CSA-1494
	public void testAddingMultipleTriggersNotifications() {
		Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE,"moscow");
		Search search = new Search(getApplication(), getElementFactory(), "Mother Russia");

		promotionService.setUpPromotion(promotion, search, 4);
		promotionsDetailPage = promotionService.goToDetails(promotion);

		String[] triggers = {"HC", "Sochi", "CKSA", "SKA", "Dinamo", "Riga"};
		promotionsDetailPage.getTriggerForm().addTrigger(StringUtils.join(triggers, ' '));

		body.getTopNavBar().notificationsDropdown();

		verifyThat(body.getTopNavBar().getNotifications().getAllNotificationMessages(), hasItem("Edited a spotlight promotion"));

		for(String notification : body.getTopNavBar().getNotifications().getAllNotificationMessages()){
			for(String trigger : triggers){
				verifyThat(notification, not(containsString(trigger)));
			}
		}
	}

	@Test
	//CSA-1769
	public void testUpdatingAndDeletingPinToPosition(){
		PinToPositionPromotion pinToPositionPromotion = new PinToPositionPromotion(1, "say anything");
		Search search = new Search(getApplication(), getElementFactory(), "Max Bemis");

		promotionService.setUpPromotion(pinToPositionPromotion, search, 2);
		promotionsDetailPage = promotionService.goToDetails(pinToPositionPromotion);

		promotionsDetailPage.pinPosition().setValueAndWait("4");
		verifyThat(promotionsDetailPage.pinPosition().getValue(), is("4"));

		String newTitle = "Admit It!!!";

		promotionsDetailPage.promotionTitle().setValueAndWait(newTitle);
		Waits.loadOrFadeWait();
		verifyThat(promotionsDetailPage.promotionTitle().getValue(), is(newTitle));

		promotionService.delete(newTitle);
		verifyThat(promotionsPage.getPromotionTitles(), not(hasItem(newTitle)));
	}

	@Test
	//CCUK-3457
	public void testPromotingItemsWithBrackets(){
		SpotlightPromotion spotlightPromotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "imagine dragons");
		Search search = new Search(getApplication(), getElementFactory(), "\"Selenium (software)\"").applyFilter(new IndexFilter("wiki_eng"));

		SearchPage searchPage = search.apply();
		assumeThat("Was expecting Selenium (Software) to be the first result",searchPage.getSearchResultTitle(1), is("Selenium (software)"));

		promotionService.setUpPromotion(spotlightPromotion, search, 1);
		PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails(spotlightPromotion);

		List<String> promotedDocuments = promotionsDetailPage.getPromotedTitles();

		verifyThat(promotedDocuments.size(), is(1));
		verifyThat(promotedDocuments.get(0), is("Selenium (software)"));
	}
}
