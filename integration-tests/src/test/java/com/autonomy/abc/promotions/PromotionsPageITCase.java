package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
//import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.promotions.CreateNewDynamicPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
//import com.autonomy.abc.selenium.page.promotions.SchedulePage;
//import com.autonomy.abc.selenium.page.search.SearchBase;
//import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsElement;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.PromotionsMatchers.triggerList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private PromotionsPage promotionsPage;
	private CreateNewDynamicPromotionsPage dynamicPromotionsPage;

	@Before
	public void setUp() throws MalformedURLException {
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		promotionsPage.deleteAllPromotions();
	}


	// TODO: refactor with Search and SpotlightPromotion classes
	private List<String> setUpCarsPromotion(int numberOfDocs) {
//		final List<String> promotedDocTitles = promotionsPage.setUpANewMultiDocPromotion("English", "cars", "Sponsored", "wheels", 2, getConfig().getType().getName());
		body.getTopNavBar().search("cars");
		SearchPage searchPage = getElementFactory().getSearchPage();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		List<String> promotedDocTitles = searchPage.createAMultiDocumentPromotion(numberOfDocs);
		CreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		createNewPromotionsPage.addSpotlightPromotion("Sponsored", "wheels");
		searchPage.waitForLoad();
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();

		promotionsPage.getPromotionLinkWithTitleContaining("wheels").click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));
		return promotedDocTitles;
	}

	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.newPromotionButton().click();
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
		assertThat(promotionsPage, not(containsText("Terms may not contain commas. Separate words and phrases with whitespace.")));
	}
}
