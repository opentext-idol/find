package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.PromotionsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.hamcrest.MatcherAssert.assertThat;

public class CreateNewPromotionsITCase extends ABCTestBase {

	public CreateNewPromotionsITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}
	private SearchPage searchPage;

	private CreateNewPromotionsPage createPromotionsPage;

	@Before
	public void setUp() {
		final TopNavBar topNavBar = body.getTopNavBar();
		topNavBar.search("fox");
		searchPage = body.getSearchPage();
		searchPage.createAPromotion();
		createPromotionsPage = body.getCreateNewPromotionsPage();
	}

	@Test
	public void testAddPinToPosition() {
		createPromotionsPage.pinToPosition().click();
		createPromotionsPage.continueButton("type").click();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select the position"));
		assertThat("Continue button is not disabled when position equals 0", createPromotionsPage.continueButton("pinToPosition").getAttribute("class").contains("disabled"));
		assertThat("Minus button is not disabled when position equals 0", createPromotionsPage.selectPositionMinusButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);

		createPromotionsPage.continueButton("pinToPosition").click();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Spotlight Triggers"));
		assertThat("Trigger add button is not disabled when text box is empty", createPromotionsPage.triggerAddButton().getAttribute("class").contains("disabled"));
		assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.promoteButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.addSearchTrigger("animal");
		assertThat("Promote button is not disabled when no triggers are added", !createPromotionsPage.promoteButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.promoteButton().click();
		new WebDriverWait(getDriver(),3).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		assertThat("modified search page not opened", getDriver().getCurrentUrl().contains("search/modified/animal"));

		final PromotionsPage promotionsPage = body.getPromotionsPage();
		promotionsPage.openPromotionWithTitleContaining("animal");
		assertThat("page does not have pin to position name", promotionsPage.getText().contains("animal"));
		assertThat("page does not have correct pin to position number", promotionsPage.getText().contains("Pinned to position: 3"));

		promotionsPage.deletePromotion();
	}

	@Test
	public void testPinToPositionSetPosition() {
		createPromotionsPage.pinToPosition().click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.modalLoadOrFadeWait();

		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);
		assertThat("Minus button is not enabled when position equals 1", !createPromotionsPage.continueButton("pinToPosition").getAttribute("class").contains("disabled"));
		assertThat("Continue button is not enabled when position equals 1", !createPromotionsPage.selectPositionMinusButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 5", createPromotionsPage.positionInputValue() == 5);

		createPromotionsPage.selectPositionMinusButton().click();
		createPromotionsPage.selectPositionMinusButton().click();
		createPromotionsPage.selectPositionMinusButton().click();
		assertThat("Pin to position value not set to 5", createPromotionsPage.positionInputValue() == 2);

		createPromotionsPage.typePositionNumber(16);
		assertThat("Pin to position value not set to 16", createPromotionsPage.positionInputValue() == 16);

		createPromotionsPage.cancelButton("pinToPosition").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
	}

	@Test
	public void testAddRemoveTriggerTerms() {
		createPromotionsPage.navigateToTriggers();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Spotlight Triggers"));
		assertThat("Trigger add button is not disabled when text box is empty", createPromotionsPage.triggerAddButton().getAttribute("class").contains("disabled"));
		assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.promoteButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.addSearchTrigger("animal");
		assertThat("Promote button is not enabled when a trigger is added", !createPromotionsPage.promoteButton().getAttribute("class").contains("disabled"));
		assertThat("animal search trigger not added", createPromotionsPage.getSearchTriggersList().contains("animal"));

		createPromotionsPage.removeSearchTrigger("animal");
		assertThat("animal search trigger not removed", !createPromotionsPage.getSearchTriggersList().contains("animal"));
		assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.promoteButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.addSearchTrigger("bushy tail");
		assertThat("Number of triggers does not equal 2", createPromotionsPage.getSearchTriggersList().size() == 2);
		assertThat("bushy search trigger not added", createPromotionsPage.getSearchTriggersList().contains("bushy"));
		assertThat("tail search trigger not added", createPromotionsPage.getSearchTriggersList().contains("tail"));

		createPromotionsPage.removeSearchTrigger("tail");
		assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("bushy search trigger not present", createPromotionsPage.getSearchTriggersList().contains("bushy"));
		assertThat("tail search trigger not removed", !createPromotionsPage.getSearchTriggersList().contains("tail"));

		createPromotionsPage.cancelButton("trigger").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
	}

}
