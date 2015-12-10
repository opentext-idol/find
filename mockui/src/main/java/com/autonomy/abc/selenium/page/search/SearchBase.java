package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.element.Checkbox;
import com.autonomy.abc.selenium.indexes.IndexesTree;
import com.autonomy.abc.selenium.util.Locator;
import com.autonomy.abc.selenium.util.Predicates;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class SearchBase extends AppElement implements AppPage {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMMMMMMMM yyyy HH:mm");

	public SearchBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	/* search results */
	public WebElement searchResultCheckbox(final int resultNumber) {
		return new WebDriverWait(getDriver(), 20)
				.withMessage("waiting for #" + resultNumber + " search result to appear")
				.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-results li:nth-child(" + resultNumber + ") label")));
	}

	public Checkbox searchCheckboxForTitle(final String docTitle) {
		WebElement element = findElement(By.cssSelector(".search-page-contents"));
		element = element.findElement(new Locator().withTagName("a").containingCaseInsensitive(docTitle));
		element = element.findElement(By.xpath("./../../.."));
		return new Checkbox(element, getDriver());
	}

	public String getSearchResultTitle(final int searchResultNumber) {
		return getSearchResult(searchResultNumber).getText();
	}

	public WebElement getSearchResult(final int searchResultNumber) {
		return new WebDriverWait(getDriver(),60)
				.withMessage("Waiting for the #" + searchResultNumber + " search result")
				.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-results li:nth-child(" + String.valueOf(searchResultNumber) + ") h3")));
	}

	public String getSearchResultDetails(final int searchResultNumber) {
		return getParent(getSearchResult(searchResultNumber)).findElement(By.cssSelector(".details")).getText();
	}

	public int visibleDocumentsCount() {
		return findElements(By.cssSelector(".search-page-contents .search-result-item")).size();
	}

	public Date getDateFromResult(final int index) throws ParseException {
		final String dateString = getParent(getSearchResult(index)).findElement(By.cssSelector(".date")).getText();
		if (dateString.isEmpty()) {
			return null;
		}
		return DATE_FORMAT.parse(dateString.split(", ")[1]);
	}

	public List<Float> getWeightsOnPage(final int numberOfPages) {
		final List<Float> weights = new ArrayList<>();
		for (int i = 1; i <= numberOfPages; i++) {
			for (final WebElement weight : findElements(By.cssSelector(".weight"))) {
				weights.add(Float.parseFloat(weight.getText().substring(8)));
			}
			javascriptClick(forwardPageButton());
		}
		return weights;
	}

	/* promotions bucket */
	public int promotedItemsCount() {
		return findElements(By.cssSelector(".promotions-bucket-document")).size();
	}

	public List<WebElement> promotionsBucketWebElements() {
		return findElements(By.xpath(".//*[contains(@class, 'promotions-bucket-document')]/.."));
	}

	public String bucketDocumentTitle(final int bucketNumber) {
		return promotionsBucket().findElement(By.cssSelector(".promotions-bucket-document:nth-child(" + bucketNumber + ')')).getText();
	}

	public WebElement promotionsBucket() {
		return findElement(By.cssSelector(".promotions-bucket-well"));
	}

	public WebElement getPromotionBucketElementByTitle(final String docTitle) {
		return findElement(By.cssSelector(".promotions-bucket-items")).findElement(new Locator().containingCaseInsensitive(docTitle));
	}

	public void deleteDocFromWithinBucket(final String docTitle) {
		for (WebElement document : promotionsBucketWebElements()) {
			if (document.getText().compareToIgnoreCase(docTitle) == 0) {
				document.findElement(By.cssSelector(".fa-close")).click();
				new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(document));
				return;
			}
		}
		throw new NoSuchElementException("promotion bucket document with title " + docTitle);
	}

	public void emptyBucket() {
		for (final WebElement bucketItem : promotionsBucketWebElements()) {
			bucketItem.findElement(By.cssSelector(".remove-bucket-item")).click();
		}
	}

	protected List<String> bucketList(final WebElement element) {
		final List<String> bucketDocTitles = new ArrayList<>();
		for (final WebElement bucketDoc : element.findElements(By.cssSelector(".promotions-bucket-document"))) {
			bucketDocTitles.add(bucketDoc.getText());
		}
		return bucketDocTitles;
	}

	public String getTopPromotedLinkTitle() {
		return findElement(By.cssSelector(".promotions .search-result-title")).getText();
	}

	public String getTopPromotedSpotlightType() {
		return findElement(By.cssSelector(".search-result .promotion-name")).getText();
	}

	/* pagination */
	public WebElement backToFirstPageButton() {
		return getParent(findElement(By.cssSelector(".pagination-nav.centre .hp-previous-chapter")));
	}

	public WebElement backPageButton() {
		return getParent(findElement(By.cssSelector(".pagination-nav.centre .hp-previous")));
	}

	public WebElement forwardToLastPageButton() {
		return getParent(findElement(By.cssSelector(".pagination-nav.centre .hp-next-chapter")));
	}

	public WebElement forwardPageButton() {
		return getParent(findElement(By.cssSelector(".pagination-nav.centre .hp-next")));
	}

	public boolean isPageActive(final int pageNumber) {
		try {
			return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//span[text()='" + String.valueOf(pageNumber) + "']/..")).getAttribute("class").contains("active");
		} catch (final NoSuchElementException e){
			return false;
		}
	}

	public int getCurrentPageNumber() {
		loadOrFadeWait();
//		new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(waitForDocLogo()));
		waitForSearchLoadIndicatorToDisappear();
		return Integer.parseInt(findElement(By.cssSelector(".btn-nav.active")).getText());
	}

	public boolean isBackToFirstPageButtonDisabled() {
		return  getParent(backToFirstPageButton()).getAttribute("class").contains("disabled");
	}

	/* indexes/databases */
	public IndexesTree indexesTree() {
		return new IndexesTree(findElement(By.cssSelector(".databases-list")), getDriver());
	}

	@Deprecated
	public WebElement getDatabasesList() {
		return findElement(By.cssSelector(".databases-list"));
	}

	public List<String> getAllDatabases() {
		return webElementListToStringList(findElements(By.cssSelector(".child-categories label")));
	}

	public List<WebElement> getDatabaseCheckboxes() {
		return findElements(By.cssSelector(".child-categories input"));
	}

	public WebElement allDatabasesCheckbox() {
		return findElement(By.cssSelector(".checkbox input[data-category-id='all']"));
	}

	@Deprecated
	public void selectDatabase(final String databaseName) {
		expandFilter(Filter.FILTER_BY);
		expandSubFilter(Filter.DATABASES);

		if (!getSelectedDatabases().contains(databaseName) ) {
			getParent(getDatabaseCheckboxes().get(getAllDatabases().indexOf(databaseName))).click();
			loadOrFadeWait();
		}
	}

	@Deprecated
	public void selectAllIndexesOrDatabases(final String type) {
		expandFilter(Filter.FILTER_BY);
		if (type.equals("Hosted")) {
			selectAllIndexes();
		} else {
			expandSubFilter(Filter.DATABASES);
			for (final WebElement checkbox : getDatabaseCheckboxes()) {
				if (!checkbox.isSelected()) {
					getParent(checkbox).click();
				}
			}
		}
		//new AppBody(getDriver()).getSearchPage().waitForSearchLoadIndicatorToDisappear();
        waitForSearchLoadIndicatorToDisappear();
	}

	@Deprecated
	public void selectAllIndexes() {
		expandFilter(Filter.FILTER_BY);
		expandSubFilter(Filter.INDEXES);
		if (!findElement(By.xpath(".//label[text()[contains(., 'All')]]/div")).getAttribute("class").contains("checked")) {
			findElement(By.xpath(".//label[text()[contains(., 'All')]]/div/ins")).click();
			waitForSearchLoadIndicatorToDisappear();
		}
	}

	@Deprecated
	public void deselectDatabase(final String databaseName) {
		final List<String> selectedDatabases = getSelectedDatabases();

		if (selectedDatabases.contains(databaseName)) {
			if (selectedDatabases.size() > 1) {
				getParent(getDatabaseCheckboxes().get(getAllDatabases().indexOf(databaseName))).click();
			} else {
				System.out.println("Only one database remaining. Can't deselect final database");
			}
			loadOrFadeWait();
		}
	}

	@Deprecated
	public List<String> getSelectedDatabases() {
		final List<String> selected = new ArrayList<>();

		for (final WebElement tick : getDatabasesList().findElements(By.cssSelector(".child-categories .checked"))) {
			selected.add(getParent(tick).getText());
		}

		return selected;
	}

	public List<Checkbox> indexList() {
		List<Checkbox> checkboxes = new ArrayList<>();
		for (WebElement element : findElements(By.cssSelector(".databases-list .checkbox"))) {
			if (element.isDisplayed()) {
				checkboxes.add(new Checkbox(element, getDriver()));
			}
		}
		return checkboxes;
	}

	public Checkbox indexCheckbox(String indexName) {
		return new Checkbox(findElement(By.cssSelector(".checkbox[data-name='" + indexName + "']")), getDriver());
	}

	public Checkbox allIndexesCheckbox() {
		return new Checkbox(findElement(By.cssSelector(".checkbox[data-category-id='all']")), getDriver());
	}

	public void deselectIndex(String index) {
		Checkbox checkbox = indexCheckbox(index);

		if(checkbox.isChecked()){
			checkbox.toggle();
		}

		loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
	}

	public void openPublicFilter(){
		findElement(By.cssSelector("[data-category-id=public] i")).click();
	}

	public void selectIndex(String index) {
		Checkbox checkbox = indexCheckbox(index);

		if(checkbox.isChecked()){
			return;
		}

		if(!checkbox.isDisplayed()){
			openPublicFilter();
			loadOrFadeWait();
		}

		checkbox.toggle();

		loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
	}

	/* date filter */
	public void openFromDatePicker() {
		findElement(By.cssSelector("[data-filter-name=\"minDate\"] .clickable")).click();
		loadOrFadeWait();
	}

	public void closeFromDatePicker() {
		if (!getDriver().findElements(By.cssSelector(".datepicker")).isEmpty()) {
			findElement(By.cssSelector("[data-filter-name=\"minDate\"] .clickable")).click();
			loadOrFadeWait();
			waitForSearchLoadIndicatorToDisappear();
		}
	}

	public WebElement fromDateTextBox() {
		return findElement(By.cssSelector("[data-filter-name=\"minDate\"] input"));
	}

	public WebElement untilDateTextBox() {
		return findElement(By.cssSelector("[data-filter-name=\"maxDate\"] input"));
	}

	public void openUntilDatePicker() {
		findElement(By.cssSelector("[data-filter-name=\"maxDate\"] .clickable")).click();
		loadOrFadeWait();
	}

	public void closeUntilDatePicker() {
		if (!getDriver().findElements(By.cssSelector(".datepicker")).isEmpty()) {
			findElement(By.cssSelector("[data-filter-name=\"maxDate\"] .clickable")).click();
			loadOrFadeWait();
			waitForSearchLoadIndicatorToDisappear();
		}
	}

	public Date getDateFromFilter(final WebElement filter) throws ParseException {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return dateFormat.parse(filter.getAttribute("value"));
	}

	public void sendDateToFilter(final Date date, final WebElement filter) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		filter.clear();
		filter.sendKeys(dateFormat.format(date));
	}

	/* related concepts */
	public void showRelatedConcepts() {
		expandFilter(Filter.RELATED_CONCEPTS);
	}

	public int countRelatedConcepts() {
		return getRelatedConcepts().size();
	}

	public List<WebElement> getRelatedConcepts() {
		return findElements(By.cssSelector(".concepts li"));
	}

	public WebElement relatedConcept(final String conceptText) {
		return findElement(By.cssSelector(".concepts")).findElement(By.xpath(".//a[text()=\"" + conceptText + "\"]"));
	}

	/* field text */
	public void setFieldText(String value) {
		showFieldTextOptions();
		try {
			fieldTextAddButton().click();
			loadOrFadeWait();
		} catch (ElementNotVisibleException e) {
			/* already clicked */
		}
		fieldTextInput().clear();
		fieldTextInput().sendKeys(value);
		fieldTextTickConfirm().click();
		waitForSearchLoadIndicatorToDisappear();
	}

	public WebElement fieldTextAddButton() {
		return findElement(By.xpath(".//button[contains(text(), 'FieldText Restriction')]"));
	}

	public WebElement fieldTextInput() {
		return findElement(By.xpath(".//input[@placeholder='FieldText']"));
	}

	public WebElement fieldTextTickConfirm() {
		return findElement(By.cssSelector(".field-text-form")).findElement(By.xpath(".//i[contains(@class, 'fa-check')]/.."));
	}

	public WebElement fieldTextEditButton() {
		return findElement(By.cssSelector(".current-field-text-container .edit-field-text"));
	}

	public WebElement fieldTextRemoveButton() {
		return findElement(By.cssSelector(".current-field-text-container .remove-field-text"));
	}

	public void clearFieldText() {
		if (!fieldTextAddButton().isDisplayed()) {
			getDatabasesList().click();
			fieldTextRemoveButton().click();
		}
	}

	public void showFieldTextOptions() {
		expandFilter(Filter.FIELD_TEXT);
	}

	/* side bar */
	private WebElement getFilter(final String filter) {
		return findElement(By.xpath(".//h4[contains(text(), '" + filter + "')]/.."));
	}

	private WebElement getSubFilter(final Filter filter) {
		return findElement(By.xpath(".//h5[contains(text(), '" + filter.getName() + "')]/.."));
	}

	public void expandFilter(final Filter filterName) {
		if (getFilter(filterName.getName()).getAttribute("class").contains("collapsed")) {
			scrollIntoView(getFilter(filterName.getName()), getDriver());
			getFilter(filterName.getName()).click();
			loadOrFadeWait();
		}
	}

	public void expandSubFilter(final Filter filterName) {
		if (getSubFilter(filterName).getAttribute("class").contains("collapsed")) {
			scrollIntoViewAndClick(getSubFilter(filterName));
			loadOrFadeWait();
		}
	}

	public enum Filter {
		FILTER_BY("Filter By"),
		RELATED_CONCEPTS("Related Concepts"),
		FIELD_TEXT("Field Text"),
		INDEXES("Indexes"),
		DATABASES("Databases"),
		DATES("Dates"),
		PARAMETRIC_VALUES("Parametric Values");

		private final String name;

		Filter(final String filterName) {
			name = filterName;
		}

		public String getName() {
			return name;
		}
	}

	/* waits */
	public WebElement waitForDocLogo() {
		return new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".fa-file-o")));
	}

	public void waitForSearchLoadIndicatorToDisappear() {
		waitForSearchLoadIndicatorToDisappear(30);
	}

	public void waitForSearchLoadIndicatorToDisappear(int seconds) {
		new WebDriverWait(getDriver(), seconds).withMessage("Search results didn't load").until(Predicates.invisibilityOfAllElementsLocated(By.className("fa-spin")));
	}

	public void waitForSynonymsLoadingIndicatorToDisappear(){
		new WebDriverWait(getDriver(),60).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				return driver.findElement(By.className("search-synonyms-loading")).getAttribute("style").contains("display: none");
			}
		});
	}

    public void waitForPromotionsLoadIndicatorToDisappear() {
		final WebElement promotionsBox = new WebDriverWait(getDriver(), 20).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".promotions")));
        new WebDriverWait(getDriver(), 60).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver input) {
				return !promotionsBox.isDisplayed() || resultsAreLoaded(promotionsBox);
			}
		});
		loadOrFadeWait();
    }

	private boolean resultsAreLoaded(WebElement promotionsBox) {
		return promotionsBox.findElements(By.cssSelector(".search-result-title")).size() > 0;
	}

	public void waitForRelatedConceptsLoadIndicatorToDisappear() {
		try {
			while (!findElement(By.cssSelector(".search-related-concepts .loading")).getAttribute("class").contains("hidden")){
				loadOrFadeWait();
			}
		} catch (final StaleElementReferenceException | org.openqa.selenium.NoSuchElementException e) {
			// Loading Complete
		}
	}

	// TODO: move all these waits into an enum
	// e.g. waitUntilLoaded(SearchBase.Section.PARAMETRIC_VALUES)
	public void waitForParametricValuesToLoad() {
		new WebDriverWait(getDriver(), 30)
				.withMessage("loading parametric values list")
				.until(new ExpectedCondition<Boolean>() {
					@Override
					public Boolean apply(WebDriver driver) {
						return !parametricValueLoadIndicator().isDisplayed();
					}
				});
	}

	public WebElement parametricValueLoadIndicator() {
		return findElement(By.cssSelector(".search-parametric .processing-indicator"));
	}

	/* general */
	public boolean isErrorMessageShowing() {
		return !findElement(By.cssSelector(".search-information")).getAttribute("class").contains("hide");
	}

	public void sortByRelevance() {
		sortBy("by relevance");
	}

	public void sortByDate() {
		sortBy("by date");
	}

	private void sortBy(final String sortBy) {
		new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".current-search-sort"))).click();

		final WebElement element = findElement(By.cssSelector(".search-results-sort")).findElement(By.xpath(".//a[text()='" + sortBy + "']"));
		// IE doesn't like clicking dropdown elements
		final JavascriptExecutor executor = (JavascriptExecutor)getDriver();
		executor.executeScript("arguments[0].click();", element);

		loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
	}

	public List<String> filterLabelList() {
		return webElementListToStringList(findElements(By.cssSelector(".filter-display-view .filter-display-text")));
	}
}
