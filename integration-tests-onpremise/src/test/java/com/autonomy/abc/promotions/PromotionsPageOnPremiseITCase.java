package com.autonomy.abc.promotions;

import com.autonomy.abc.base.SOTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.promotions.OPPromotionService;
import com.autonomy.abc.selenium.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.query.FieldTextFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;


public class PromotionsPageOnPremiseITCase extends SOTestBase {

	public PromotionsPageOnPremiseITCase(final TestConfig config) {
		super(config);
	}

	private OPPromotionsDetailPage promotionsDetailPage;
	private SearchPage searchPage;
    private OPPromotionService promotionService;
    private SearchService searchService;

	@Before
	public void setUp() throws MalformedURLException {
		promotionService = (OPPromotionService) getApplication().promotionService();
        searchService = getApplication().searchService();

		promotionService.deleteAll();
	}

    private void search(String term) {
        searchPage = searchService.search(term);
    }

	@Test
	public void testInvalidFieldText() {
		Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "hot");
		promotionService.setUpPromotion(promotion, "hot", 1);
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
        promotionService.setUpPromotion(promotion, "hot", 1);

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
		promotionService.setUpPromotion(promotion, "road", 1);
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
		promotionService.setUpPromotion(promotion, "road", 1);
		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.fieldTextAddButton().click();
		Waits.loadOrFadeWait();
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
        Query query = new Query("flash").withFilter(filter);

		promotionService.setUpPromotion(promotion, query, 1);
        promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.addFieldText(fieldText);
		verifyNotDisplayed("ming");

        // filter is cached on search page
		filter.clear(searchPage);
		verifyThat(searchPage.promotionsSummary(), displayed());
	}
}
