package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.Removable;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.OPPromotionService;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.FieldTextFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;


public class PromotionsPageOnPremiseITCase extends ABCTestBase {

	public PromotionsPageOnPremiseITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private OPPromotionsPage promotionsPage;
	private OPPromotionsDetailPage promotionsDetailPage;
	private SearchPage searchPage;
    private OPPromotionService promotionService;
    private SearchActionFactory searchActionFactory;

	@Before
	public void setUp() throws MalformedURLException {
		promotionService = (OPPromotionService) getApplication().createPromotionService(getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());

		promotionsPage = promotionService.deleteAll();
	}

    private void search(String term) {
        searchPage = searchActionFactory.makeSearch(term).apply();
    }

	@Test
	public void testInvalidFieldText() {
		Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "hot");
		Search search = searchActionFactory.makeSearch("hot");
		promotionService.setUpPromotion(promotion, search, 1);
		List<String> badValues = Arrays.asList("", "bad", "<h1>h1</h1>");

		promotionsDetailPage = promotionService.goToDetails(promotion);
		FormInput inputBox = promotionsDetailPage.fieldTextInput();
		Editable editableFieldText = promotionsDetailPage.editableFieldText();

		for (String badValue : badValues) {
			promotionsDetailPage.fieldTextAddButton().click();
			verifyThat("error message not visible", promotionsDetailPage.getFieldTextError(), isEmptyOrNullString());
			inputBox.setAndSubmit(badValue);
			verifyThat("cannot add field text '" + badValue + "'", promotionsDetailPage.getFieldTextError(), containsString("SyntaxError"));
			promotionsDetailPage.closeInputBox();
		}

		String goodText = "MATCH{good}:DRECONTENT";
		promotionsDetailPage.addFieldText(goodText);
		verifyThat("field text added", editableFieldText.getValue(), is(goodText));

		for (String badValue : badValues) {
			editableFieldText.editButton().click();
			verifyThat("error message not visible", promotionsDetailPage.getFieldTextError(), isEmptyOrNullString());
			inputBox.setAndSubmit(badValue);
			verifyThat("cannot set field text to '" + badValue + "'", promotionsDetailPage.getFieldTextError(), containsString("SyntaxError"));
			promotionsDetailPage.closeInputBox();
		}

		goodText = "NOT MATCH{bad}:DRECONTENT";
		promotionsDetailPage.updateFieldText(goodText);
		verifyThat("field text updated", editableFieldText.getValue(), is(goodText));

		promotionsDetailPage.removeFieldText();
		verifyThat("field text removed", promotionsDetailPage.fieldTextAddButton(), displayed());
	}

	private void verifyDisplayed(String searchTerm) {
		search(searchTerm);
		verifyThat("promotion displayed for search term '" + searchTerm + "'", searchPage.promotionsSummary(), displayed());
	}

	private void verifyNotDisplayed(String searchTerm) {
		search(searchTerm);
		verifyThat("promotion not displayed for search term '" + searchTerm + "'", searchPage.promotionsSummary(), not(displayed()));
	}

	@Test
	public void testPromotionFieldTextRestriction() {
        Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "hot");
        Search search = searchActionFactory.makeSearch("hot");
        promotionService.setUpPromotion(promotion, search, 1);

        promotionsDetailPage = promotionService.goToDetails(promotion);
		promotionsDetailPage.addFieldText("MATCH{hot}:DRECONTENT");

		verifyDisplayed("hot");
		for (String badValue : Arrays.asList("hot pot", "hots")) {
			verifyNotDisplayed(badValue);
		}

		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.removableFieldText().removeAndWait();

		for (String goodValue : Arrays.asList("hot", "hot chocolate", "hots")) {
			verifyDisplayed(goodValue);
		}

		promotionsDetailPage = promotionService.goToDetails(promotion);
		promotionsDetailPage.addFieldText("MATCH{temporary value}:DRECONTENT");
		promotionsDetailPage.editableFieldText().setValueAndWait("MATCH{hot dog}:DRECONTENT");

		verifyDisplayed("hot dog");
		for (String badValue : Arrays.asList("hot chocolate", "hot", "dog", "hot dogs")) {
			verifyNotDisplayed(badValue);
		}
    }

	@Test
	public void testPromotionFieldTextOrRestriction() {
		Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "highway street");
		promotionService.setUpPromotion(promotion, searchActionFactory.makeSearch("road"), 1);
		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.addFieldText("MATCH{highway}:DRECONTENT OR MATCH{street}:DRECONTENT");

		for (String goodTerm : Arrays.asList("highway", "street")) {
			verifyDisplayed(goodTerm);
		}
		for (String badTerm : Arrays.asList("highway street", "road", "ROAD", "highway street", "street highway", "street street", "highwaystreet", "highway AND street", "highway OR street")) {
			verifyNotDisplayed(badTerm);
		}
	}

	@Test
	public void testFieldTextSubmitTextOnEnter() {
		Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "highway street");
		promotionService.setUpPromotion(promotion, searchActionFactory.makeSearch("road"), 1);
		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.fieldTextAddButton().click();
		promotionsPage.loadOrFadeWait();
		promotionsDetailPage.fieldTextInput().setValue("MATCH{test}:DRECONTENT" + Keys.RETURN);
		promotionsDetailPage.waitForFieldTextToUpdate();
		verifyThat("Added field text with RETURN", promotionsDetailPage.editableFieldText().getValue(), not(isEmptyOrNullString()));
	}

    // fails: CCUK-3250?
	@Test
	public void testCreateFieldTextField() {
        Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "ming");
        String fieldText = "RANGE{.,01/01/2010}:DREDATE";
        FieldTextFilter filter = new FieldTextFilter(fieldText);
        Search search = searchActionFactory.makeSearch("flash");//.applyFilter(filter);
		searchPage = search.apply();
		filter.apply(searchPage);
		filter.clear(searchPage);
		filter.clear(searchPage);

		promotionService.setUpPromotion(promotion, search, 1);
        promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.addFieldText(fieldText);
		verifyNotDisplayed("ming");

		filter.clear(searchPage);
		verifyThat(searchPage.promotionsSummary(), displayed());
	}
}
