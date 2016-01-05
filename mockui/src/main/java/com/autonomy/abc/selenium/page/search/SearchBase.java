package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.element.*;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.search.DatePickerFilter;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchFilter;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.search.StringDateFilter;
import com.autonomy.abc.selenium.util.Locator;
import com.autonomy.abc.selenium.util.Predicates;
import com.autonomy.abc.selenium.util.Waits;
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

public abstract class SearchBase extends AppElement implements AppPage,
		SearchFilter.Filterable,
		IndexFilter.Filterable,
		DatePickerFilter.Filterable,
		StringDateFilter.Filterable {

	private static final SimpleDateFormat RESULT_DATE_FORMAT = new SimpleDateFormat("dd MMMMMMMMM yyyy HH:mm");

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
		return ElementUtil.getParent(getSearchResult(searchResultNumber)).findElement(By.cssSelector(".details")).getText();
	}

	public int visibleDocumentsCount() {
		return findElements(By.cssSelector(".search-page-contents .search-result-item")).size();
	}

	public Date getDateFromResult(final int index) throws ParseException {
		final String dateString = ElementUtil.getParent(getSearchResult(index)).findElement(By.cssSelector(".date")).getText();
		if (dateString.isEmpty()) {
			return null;
		}
		return RESULT_DATE_FORMAT.parse(dateString.split(", ")[1]);
	}

	public List<Float> getWeightsOnPage(final int numberOfPages) {
		final List<Float> weights = new ArrayList<>();
		for (int i = 1; i <= numberOfPages; i++) {
			for (final WebElement weight : findElements(By.cssSelector(".weight"))) {
				weights.add(Float.parseFloat(weight.getText().substring(8)));
			}
			ElementUtil.javascriptClick(forwardPageButton(), getDriver());
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
	@Deprecated
	public WebElement backToFirstPageButton() {
		return resultsPaginationButton(Pagination.FIRST);
	}

	@Deprecated
	public WebElement backPageButton() {
		return resultsPaginationButton(Pagination.PREVIOUS);
	}

	@Deprecated
	public WebElement forwardToLastPageButton() {
		return resultsPaginationButton(Pagination.LAST);
	}

	@Deprecated
	public WebElement forwardPageButton() {
		return resultsPaginationButton(Pagination.NEXT);
	}

	public int getCurrentPageNumber() {
		Waits.loadOrFadeWait();
//		new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(waitForDocLogo()));
		waitForSearchLoadIndicatorToDisappear();
		return Integer.parseInt(findElement(By.cssSelector(".btn-nav.active")).getText());
	}

	public boolean isBackToFirstPageButtonDisabled() {
		return  ElementUtil.getParent(backToFirstPageButton()).getAttribute("class").contains("disabled");
	}

	public void switchResultsPage(Pagination pagination) {
		resultsPaginationButton(pagination).click();
		waitForSearchLoadIndicatorToDisappear();
	}

	public WebElement resultsPaginationButton(Pagination pagination) {
		return pagination.findInside(resultsPagination());
	}

	private WebElement resultsPagination() {
		return findElement(By.cssSelector(".search-results-view .pagination-nav"));
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
		return ElementUtil.webElementListToStringList(findElements(By.cssSelector(".child-categories label")));
	}

	public List<WebElement> getDatabaseCheckboxes() {
		return findElements(By.cssSelector(".child-categories input"));
	}

	public WebElement allDatabasesCheckbox() {
		return findElement(By.cssSelector(".checkbox input[data-category-id='all']"));
	}

	@Deprecated
	public void selectDatabase(final String databaseName) {
		indexesTree().select(databaseName);
		Waits.loadOrFadeWait();
	}

	@Deprecated
	public void selectAllIndexesOrDatabases(final String type) {
		selectAllIndexes();
	}

	@Deprecated
	public void selectAllIndexes() {
		expand(Facet.FILTER_BY);
		expand(Facet.INDEXES);
		indexesTree().allIndexes().select();
		waitForSearchLoadIndicatorToDisappear();
	}

	@Deprecated
	public void deselectDatabase(final String databaseName) {
		final List<String> selectedDatabases = getSelectedDatabases();

		if (selectedDatabases.contains(databaseName)) {
			if (selectedDatabases.size() > 1) {
				ElementUtil.getParent(getDatabaseCheckboxes().get(getAllDatabases().indexOf(databaseName))).click();
			} else {
				System.out.println("Only one database remaining. Can't deselect final database");
			}
			Waits.loadOrFadeWait();
		}
	}

	@Deprecated
	public List<String> getSelectedDatabases() {
		final List<String> selected = new ArrayList<>();

		for (final WebElement tick : getDatabasesList().findElements(By.cssSelector(".child-categories .checked"))) {
			selected.add(ElementUtil.getParent(tick).getText());
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

		Waits.loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
	}

	public void openPublicFilter(){
		WebElement publicChevron = findElement(By.cssSelector("[data-category-id=public] i"));
		if(publicChevron.getAttribute("class").contains("collapsed")) {
			publicChevron.click();
		}
	}

	public void selectIndex(String index) {
		Checkbox checkbox = indexCheckbox(index);

		if(checkbox.isChecked()){
			return;
		}

		if(!checkbox.isDisplayed()){
			openPublicFilter();
			Waits.loadOrFadeWait();
		}

		checkbox.toggle();

		Waits.loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
	}

	/* date filter */
	public FormInput fromDateInput() {
		return dateInput(By.cssSelector("[data-filter-name=\"minDate\"] input"));
	}

	public FormInput untilDateInput() {
		return dateInput(By.cssSelector("[data-filter-name=\"maxDate\"] input"));
	}

	private FormInput dateInput(By locator) {
		expand(Facet.FILTER_BY);
		expand(Facet.DATES);
		findElement(By.cssSelector("[data-filter-name=\"maxDate\"] .clickable")).click();
		Waits.loadOrFadeWait();
		WebElement textBox = findElement(locator);
		return new FormInput(textBox, getDriver());
	}

	public DatePicker fromDatePicker() {
		return datePicker(By.cssSelector("[data-filter-name='minDate']"));
	}

	public DatePicker untilDatePicker() {
		return datePicker(By.cssSelector("[data-filter-name='maxDate']"));
	}

	private DatePicker datePicker(By locator) {
		expand(Facet.FILTER_BY);
		expand(Facet.DATES);
		return new DatePicker(findElement(locator), getDriver());
	}

	/* related concepts */
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
		expand(Facet.FIELD_TEXT);
		try {
			fieldTextAddButton().click();
			Waits.loadOrFadeWait();
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
		expand(Facet.FIELD_TEXT);
		if (!fieldTextAddButton().isDisplayed()) {
			getDatabasesList().click();
			fieldTextRemoveButton().click();
		}
	}

	public void expand(FacetFilter section) {
		section.findInside(this).expand();
	}

	public void collapse(FacetFilter section) {
		section.findInside(this).collapse();
	}

	protected interface FacetFilter {
		Collapsible findInside(WebElement container);
	}

	public enum Facet implements FacetFilter {
		KEYWORDS("h4", "Keywords"),
		FILTER_BY("h4", "Filter By"),
			INDEXES(By.cssSelector(".search-databases>a")),
			DATES("h5", "Dates"),
			PARAMETRIC_VALUES("h5", "Parametric Values"),
		RELATED_CONCEPTS("h4", "Related Concepts"),
		FIELD_TEXT("h4", "Field Text");

		private final By locator;

		Facet(String tagName, String content) {
			this(getFacetLocator(tagName, content));
		}

		Facet(By by) {
			locator = by;
		}

		@Override
		public Collapsible findInside(WebElement container) {
			return new ChevronContainer(container.findElement(locator));
		}
	}

	protected static By getFacetLocator(final String tagName, final String content) {
		return By.xpath(".//" + tagName + "[contains(text(), '" + content + "')]/../..");
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
		Waits.loadOrFadeWait();
    }

	private boolean resultsAreLoaded(WebElement promotionsBox) {
		return promotionsBox.findElements(By.cssSelector(".search-result-title")).size() > 0;
	}

	public void waitForRelatedConceptsLoadIndicatorToDisappear() {
		try {
			while (!findElement(By.cssSelector(".search-related-concepts .loading")).getAttribute("class").contains("hidden")){
				Waits.loadOrFadeWait();
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

	public enum Sort {
		DATE("by date"),
		RELEVANCE("by relevance");

		private final String name;

		Sort(String content) {
			name = content;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public List<String> filterLabelList() {
		return ElementUtil.webElementListToStringList(findElements(By.cssSelector(".filter-display-view .filter-display-text")));
	}

	public void filterBy(SearchFilter filter) {
		filter.apply(this);
		waitForSearchLoadIndicatorToDisappear();
	}
}
