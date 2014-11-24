package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.PromotionsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private TopNavBar topNavBar;
	private PromotionsPage promotionsPage;
	private SearchPage searchPage;
	private String promotedDocTitle;
	private CreateNewPromotionsPage createPromotionsPage;

	@Before
	public void setUp() throws MalformedURLException {
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.newPromotionButton().click();
		assertThat("linked to wrong page", getDriver().getCurrentUrl().endsWith("promotions/new"));
		assertThat("linked to wrong page", body.getText().contains("Create New Promotion"));
	}

	@Test
	public void testCorrectDocumentsInPromotion() {
		topNavBar = body.getTopNavBar();
		topNavBar.search("car");
		searchPage = body.getSearchPage();
		final List<String> promotedDocTitles = searchPage.createAMultiDocumentPromotion();
		createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "wheels");

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		final PromotionsPage promotionsPage = new AppBody(getDriver()).getPromotionsPage();
		promotionsPage.openPromotionWithTitleContaining("wheels");
		final List<String> promotedList = promotionsPage.getPromotedList();

		for (final String title : promotedDocTitles) {
			assertThat("Promoted document title '" + title + "' does not match promoted documents on promotions page", promotedList.contains(title));
		}
	}

	@Test
	public void testWhitespaceTrigger() {
		setUpANewPromotion();

		promotionsPage.triggerAddButton().click();
		assertThat("Number of triggers does not equal 0", promotionsPage.getSearchTriggersList().size() == 1);

		promotionsPage.addSearchTrigger("trigger");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("   ");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger(" trigger");
		assertThat("Whitespace at beginning should be ignored", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("\t");
		assertThat("Whitespace at beginning should be ignored", promotionsPage.getSearchTriggersList().size() == 2);
	}

	@Test
	public void testQuotesTrigger() throws InterruptedException {
		setUpANewPromotion();

		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);

		promotionsPage.addSearchTrigger("bag");
		promotionsPage.addSearchTrigger("\"bag");
		promotionsPage.waitForGritterToClear();
		promotionsPage.addSearchTrigger("bag\"");
		promotionsPage.addSearchTrigger("\"bag\"");
		assertThat("Number of triggers does not equal 5", promotionsPage.getSearchTriggersList().size() == 5);

		promotionsPage.removeSearchTrigger("\"bag\"");
		assertThat("Number of triggers does not equal 4", promotionsPage.getSearchTriggersList().size() == 4);

		promotionsPage.removeSearchTrigger("\"bag");
		assertThat("Number of triggers does not equal 3", promotionsPage.getSearchTriggersList().size() == 3);

		promotionsPage.removeSearchTrigger("bag\"");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.removeSearchTrigger("bag");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);
	}

	@Test
	public void testCommasTrigger() {
		setUpANewPromotion();

		promotionsPage.addSearchTrigger("France");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger(",Germany");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("Ita,ly Spain");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 3);

		promotionsPage.addSearchTrigger("Ireland, Belgium");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 4);

		promotionsPage.addSearchTrigger("UK , Luxembourg");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 6);
	}

	@Test
	public void testScriptTrigger() {
		setUpANewPromotion();

		promotionsPage.addSearchTrigger("<script> alert(\"We don't want this to happen\") </script>");
		assertThat("Scripts should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 9);
	}

	@Test
	public void testAddRemoveTriggers() throws InterruptedException {
		setUpANewPromotion();
		promotionsPage.addSearchTrigger("alpha");
		promotionsPage.removeSearchTrigger("wheels");
		assertThat("Number of search terms does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Original search trigger 'wheels has not been deleted'", !promotionsPage.getSearchTriggersList().contains("wheels"));

		promotionsPage.addSearchTrigger("beta gamma delta");
		promotionsPage.waitForGritterToClear();
		promotionsPage.removeSearchTrigger("gamma");
		promotionsPage.removeSearchTrigger("alpha");
		promotionsPage.addSearchTrigger("epsilon");
		promotionsPage.removeSearchTrigger("beta");

		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("Trigger 'delta' not present", promotionsPage.getSearchTriggersList().contains("delta"));
		assertThat("Trigger 'epsilon' not present", promotionsPage.getSearchTriggersList().contains("epsilon"));
	}

	private String setUpANewPromotion() {
		topNavBar = body.getTopNavBar();
		topNavBar.search("car");
		searchPage = body.getSearchPage();
		promotedDocTitle = searchPage.createAPromotion();
		createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "wheels");

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		final PromotionsPage promotionsPage = new AppBody(getDriver()).getPromotionsPage();
		promotionsPage.openPromotionWithTitleContaining("wheels");
		return promotedDocTitle;
	}
}
