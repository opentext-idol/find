package com.autonomy.abc.promotions;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.selenium.promotions.IdolPromotionService;
import com.autonomy.abc.selenium.promotions.IdolPromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Editable;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;


public class PromotionsPageOnPremiseITCase extends IdolIsoTestBase {

	public PromotionsPageOnPremiseITCase(final TestConfig config) {
		super(config);
	}

	private IdolPromotionsDetailPage promotionsDetailPage;
	private SearchPage searchPage;
    private IdolPromotionService promotionService;
    private SearchService searchService;

	@Before
	public void setUp() throws MalformedURLException {
		promotionService = getApplication().promotionService();
        searchService = getApplication().searchService();

		promotionService.deleteAll();
	}

    private void search(final String term) {
        searchPage = searchService.search(term);
    }

	@Test
	public void testInvalidFieldText() {
		final Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "hot");
		promotionService.setUpPromotion(promotion, "hot", 1);
		final List<String> badValues = Arrays.asList("", "bad", "<h1>h1</h1>");

		promotionsDetailPage = promotionService.goToDetails(promotion);
		final FormInput inputBox = promotionsDetailPage.fieldTextInput();
		final Editable editableFieldText = promotionsDetailPage.editableFieldText();

		for (final String badValue : badValues) {
			promotionsDetailPage.fieldTextAddButton().click();
			verifyThat("error message not visible", promotionsDetailPage.getFieldTextError(), isEmptyOrNullString());
			inputBox.setAndSubmit(badValue);
			verifyThat("cannot add field text '" + badValue + '\'', promotionsDetailPage.getFieldTextError(), containsString("SyntaxError"));
			promotionsDetailPage.closeFieldTextBox();
		}

		String goodText = "MATCH{good}:DRECONTENT";
		promotionsDetailPage.addFieldText(goodText);
		verifyThat("field text added", editableFieldText.getValue(), is(goodText));

		for (final String badValue : badValues) {
			editableFieldText.editButton().click();
			verifyThat("error message not visible", promotionsDetailPage.getFieldTextError(), isEmptyOrNullString());
			inputBox.setAndSubmit(badValue);
			verifyThat("cannot set field text to '" + badValue + '\'', promotionsDetailPage.getFieldTextError(), containsString("SyntaxError"));
			promotionsDetailPage.closeFieldTextBox();
		}

		goodText = "NOT MATCH{bad}:DRECONTENT";
		promotionsDetailPage.updateFieldText(goodText);
		verifyThat("field text updated", editableFieldText.getValue(), is(goodText));

		promotionsDetailPage.removeFieldText();
		verifyThat("field text removed", promotionsDetailPage.fieldTextAddButton(), displayed());
	}

	private void verifyDisplayed(final String searchTerm) {
		search(searchTerm);
		Waits.loadOrFadeWait();
		verifyThat("promotion displayed for search term '" + searchTerm + '\'', searchPage.promotionsSummary(), displayed());
	}

	private void verifyNotDisplayed(final String searchTerm) {
		search(searchTerm);
		verifyThat("promotion not displayed for search term '" + searchTerm + '\'', searchPage.promotionsSummary(), not(displayed()));
	}

	@Test
	public void testPromotionFieldTextRestriction() {
        final Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "hot");
        promotionService.setUpPromotion(promotion, "hot", 1);

        promotionsDetailPage = promotionService.goToDetails(promotion);
		promotionsDetailPage.addFieldText("MATCH{hot}:DRECONTENT");

		verifyDisplayed("hot");
		for (final String badValue : Arrays.asList("hot pot", "hots")) {
			verifyNotDisplayed(badValue);
		}

		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.removableFieldText().removeAndWait();

		for (final String goodValue : Arrays.asList("hot", "hot chocolate", "hots")) {
			verifyDisplayed(goodValue);
		}

		promotionsDetailPage = promotionService.goToDetails(promotion);
		promotionsDetailPage.addFieldText("MATCH{temporary value}:DRECONTENT");
		promotionsDetailPage.editableFieldText().setValueAndWait("MATCH{hot dog}:DRECONTENT");

		verifyDisplayed("hot dog");
		for (final String badValue : Arrays.asList("hot chocolate", "hot", "dog", "hot dogs")) {
			verifyNotDisplayed(badValue);
		}
    }

	@Test
	public void testPromotionFieldTextOrRestriction() {
		final Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "highway street");
		promotionService.setUpPromotion(promotion, "road", 1);
		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.addFieldText("MATCH{highway}:DRECONTENT OR MATCH{street}:DRECONTENT");

		for (final String goodTerm : Arrays.asList("highway", "street")) {
			verifyDisplayed(goodTerm);
		}
		for (final String badTerm : Arrays.asList("highway street", "road", "ROAD", "highway street", "street highway", "street street", "highwaystreet", "highway AND street", "highway OR street")) {
			verifyNotDisplayed(badTerm);
		}
	}

	@Test
	public void testPromotionFieldTextAndRestriction() {
		final Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "highway street");
		promotionService.setUpPromotion(promotion, "road", 1);
		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.addFieldText("MATCH{highway street}:DRECONTENT");

		for (final String badTerm : Arrays.asList("highway", "street", "road", "ROAD", "street street", "street highway", "highwaystreet")) {
			verifyNotDisplayed(badTerm);
		}
		verifyDisplayed("highway street");
	}

	@Test
	public void testFieldTextSubmitTextOnEnter() {
		final Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "highway street");
		promotionService.setUpPromotion(promotion, "road", 1);
		promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.fieldTextAddButton().click();
		Waits.loadOrFadeWait();
		promotionsDetailPage.fieldTextInput().setValue("MATCH{test}:DRECONTENT" + Keys.RETURN);
		promotionsDetailPage.waitForFieldTextToUpdate();
		verifyThat("Added field text with RETURN", promotionsDetailPage.editableFieldText().getValue(), not(isEmptyOrNullString()));
	}

	@Test
	public void testCreateFieldTextField() {
        final Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "best mediocre worse worser worstest");

		promotionService.setUpPromotion(promotion, "unreal", 1);
        promotionsDetailPage = promotionService.goToDetails(promotion);

		promotionsDetailPage.addFieldText("MATCH{best}:DRECONTENT");
		verifyDisplayed("best");
		for (final String badTerm : Arrays.asList("mediocre", "worse", "worser", "worstest")) {
			verifyNotDisplayed(badTerm);
		}

	}
}
