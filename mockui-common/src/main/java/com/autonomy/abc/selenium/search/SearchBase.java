package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.query.*;
import com.hp.autonomy.frontend.selenium.element.*;
import com.hp.autonomy.frontend.selenium.util.*;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class SearchBase extends SOPageBase implements
		QueryFilter.Filterable,
		IndexFilter.Filterable,
		DatePickerFilter.Filterable,
		StringDateFilter.Filterable,
		ParametricFilter.Filterable,
		QueryResultsPage {

	private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	public static final SimpleDateFormat RESULT_DATE_FORMAT = new SimpleDateFormat("dd MMMMMMMMM yyyy HH:mm");

	public SearchBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public WebElement errorContainer() {
		return findElement(By.cssSelector(".search-results-view .search-information"));
	}

	/* search results */
	public List<SOSearchResult> getSearchResults() {
		List<SOSearchResult> results = new ArrayList<>();
		for(WebElement result : findElements(By.cssSelector(".search-results li"))){
			results.add(new SOSearchResult(result, getDriver()));
		}
		return results;
	}

	public SOSearchResult getSearchResult(final int searchResult) {
		return new SOSearchResult(findElement(By.cssSelector(".search-results li:nth-child(" + searchResult + ")")), getDriver());
	}

	public Checkbox searchResultCheckbox(final int resultNumber) {
		return new SOCheckbox(searchResultCheckboxElement(resultNumber), getDriver());
	}

	protected WebElement searchResultCheckboxElement(final int resultNumber) {
		return new WebDriverWait(getDriver(), 20)
				.withMessage("waiting for #" + resultNumber + " search result to appear")
				.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-results li:nth-child(" + resultNumber + ") label")));
	}

	public Checkbox searchCheckboxForTitle(final String docTitle) {
		WebElement element = findElement(By.cssSelector(".search-page-contents"));
		element = ElementUtil.ancestor(element.findElement(new Locator().withTagName("a").containingCaseInsensitive(docTitle)), 3);
		return new SOCheckbox(element, getDriver());
	}

	public int visibleDocumentsCount() {
		return findElements(By.cssSelector(".search-page-contents .search-result-item")).size();
	}

	public List<Float> getWeightsOnPage(final int numberOfPages) {
		final List<Float> weights = new ArrayList<>();
		for (int i = 1; i <= numberOfPages; i++) {
			for (final WebElement weight : findElements(By.cssSelector(".weight"))) {
				weights.add(Float.parseFloat(weight.getText().substring(8)));
			}
			switchResultsPage(Pagination.NEXT);
		}
		return weights;
	}

	/* promotions bucket */
	public List<String> addDocsToBucket(int finalNumberOfDocs) {
		final List<String> promotedDocTitles = new ArrayList<>();
		for (int i = 0; i < finalNumberOfDocs; i++) {
			final int checkboxIndex = i % SearchPage.RESULTS_PER_PAGE + 1;
			addDocToBucket(checkboxIndex);
			promotedDocTitles.add(getSearchResult(checkboxIndex).getTitleString());

			// Change page when we have checked all boxes on the current page, if we have more to check
			if (i < finalNumberOfDocs - 1 && checkboxIndex == SearchPage.RESULTS_PER_PAGE) {
				switchResultsPage(Pagination.NEXT);
			}
		}
		return promotedDocTitles;
	}

	public void addDocToBucket(int docNumber) {
		DriverUtil.scrollIntoView(getDriver(), searchResultCheckbox(docNumber));
		searchResultCheckbox(docNumber).check();
	}

	public void removeDocFromBucket(int docNumber) {
		DriverUtil.scrollIntoView(getDriver(), searchResultCheckbox(docNumber));
		searchResultCheckbox(docNumber).uncheck();
	}

	public List<String> getBucketTitles() {
		return ElementUtil.getTexts(promotionsBucketWebElements());
	}

	private List<WebElement> promotionsBucketWebElements() {
		return findElements(By.xpath(".//*[contains(@class, 'promotions-bucket-document')]/.."));
	}

	public WebElement promotionsBucket() {
		return findElement(By.cssSelector(".promotions-bucket-well"));
	}

	public WebElement promotionBucketElementByTitle(final String docTitle) {
		return findElement(By.cssSelector(".promotions-bucket-items")).findElement(new Locator().containingCaseInsensitive(docTitle));
	}

	public void deleteDocFromWithinBucket(final String docTitle) {
		DriverUtil.scrollIntoView(getDriver(), promotionsBucket());
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
		DriverUtil.scrollIntoView(getDriver(), promotionsBucket());
		for (final WebElement bucketItem : promotionsBucketWebElements()) {
			bucketItem.findElement(By.cssSelector(".remove-bucket-item")).click();
		}
	}

	public String getTopPromotedLinkTitle() {
		return findElement(By.cssSelector(".promotions .search-result-title")).getText();
	}

	public String getTopPromotedSpotlightType() {
		return findElement(By.cssSelector(".search-result .promotion-name")).getText();
	}

	/* results pagination */
	public int getCurrentPageNumber() {
		Waits.loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
		return Integer.parseInt(findElement(By.cssSelector(".btn-nav.active")).getText());
	}

	public void switchResultsPage(Pagination pagination) {
		DriverUtil.scrollIntoViewAndClick(getDriver(), resultsPaginationButton(pagination));
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

	@Override
	public String formatInputDate(Date date) {
		return INPUT_DATE_FORMAT.format(date);
	}

	/* related concepts */
	public List<String> getRelatedConcepts() {
		return ElementUtil.getTexts(relatedConcepts());
	}

	public List<WebElement> relatedConcepts() {
		return findElements(By.cssSelector(".concepts li"));
	}

	public WebElement relatedConcept(final String conceptText) {
		return findElement(By.cssSelector(".concepts")).findElement(By.xpath(".//a[text()=\"" + conceptText + "\"]"));
	}

	/* field text */
	public WebElement fieldTextAddButton() {
		WebElement addButton = findElement(By.xpath(".//button[contains(text(), 'FieldText Restriction')]"));
		DriverUtil.scrollIntoView(getDriver(), addButton);
		return addButton;
	}

	public FormInput fieldTextInput() {
		return new FormInput(findElement(By.xpath(".//input[@placeholder='FieldText']")), getDriver());
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
			fieldTextRemoveButton().click();
		}
	}

	public void expand(FacetFilter section) {
		section.findInside(this, getDriver()).expand();
	}

	public void collapse(FacetFilter section) {
		section.findInside(this, getDriver()).collapse();
	}

	protected interface FacetFilter {
		Collapsible findInside(WebElement container, WebDriver driver);
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
		public Collapsible findInside(WebElement container, WebDriver driver) {
			return new ChevronContainer(container.findElement(locator), driver);
		}
	}

	protected static By getFacetLocator(final String tagName, final String content) {
		return By.xpath(".//" + tagName + "[contains(text(), '" + content + "')]/../..");
	}

	/* waits */
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
		return !findElement(By.cssSelector(".search-information")).getAttribute("class").contains("hidden");
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
		return ElementUtil.getTexts(findElements(By.cssSelector(".filter-display-view .filter-display-text")));
	}

	public void filterBy(QueryFilter filter) {
		filter.apply(this);
		Waits.loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
	}

	@Override
	public WebElement parametricContainer() {
		expand(Facet.FILTER_BY);
		expand(Facet.PARAMETRIC_VALUES);
		return findElement(By.className("collapsible-parametric-option"));
	}
}
