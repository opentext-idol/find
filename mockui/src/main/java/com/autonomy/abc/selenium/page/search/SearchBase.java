package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.page.AppPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsBase;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class SearchBase extends KeywordsBase implements AppPage {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMMMMMMMM yyyy HH:mm");

	public SearchBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public WebElement searchResultCheckbox(final int resultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(resultNumber) + ") .icheckbox_square-blue"));
	}

	public WebElement getResultsBoxByTitle(final String docTitle) {
		return findElement(By.xpath(".//a[contains(text(), '" + docTitle + "')]/../../../div/div/label/div"));
	}

	public WebElement searchResultCheckbox(final String docTitle) {
		return getResultsBoxByTitle(docTitle);
	}

	public String getSearchResultTitle(final int searchResultNumber) {
		return getSearchResult(searchResultNumber).getText();
	}

	public WebElement getSearchResult(final int searchResultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(searchResultNumber) + ") h3"));
	}

	public String getSearchResultDetails(final int searchResultNumber) {
		return getParent(getSearchResult(searchResultNumber)).findElement(By.cssSelector(".details")).getText();
	}

	public int promotedItemsCount() {
		return findElements(By.cssSelector(".promotions-bucket-document")).size();
	}

	public List<WebElement> promotionsBucketWebElements() {
		return findElements(By.xpath(".//*[contains(@class, 'promotions-bucket-document')]/.."));
	}

	public String bucketDocumentTitle(final int bucketNumber) {
		return promotionsBucket().findElement(By.cssSelector(".promotions-bucket-document:nth-child(" + bucketNumber + ")")).getText();
	}

	public WebElement promotionsBucket() {
		return findElement(By.cssSelector(".promotions-bucket-well"));
	}

	public void deleteDocFromWithinBucket(final String docTitle) {
		final String xpathString = cleanXpathString(docTitle);
        LoggerFactory.getLogger(SearchBase.class).info(xpathString);
		promotionsBucket().findElement(By.xpath(".//*[contains(text(), " + xpathString + ")]/../i")).click();
		loadOrFadeWait();
	}

	public WebElement backToFirstPageButton() {
		return getParent(findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-double-left')]")));
	}

	public WebElement backPageButton() {
		return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-left')]/.."));
	}

	public WebElement forwardToLastPageButton() {
		return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-double-right')]/.."));
	}

	public WebElement forwardPageButton() {
		return findElement(By.cssSelector(".pagination-nav.centre")).findElement(By.xpath(".//i[contains(@class, 'fa-angle-right')]/.."));
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
		new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(waitForDocLogo()));
		return Integer.parseInt(findElement(By.cssSelector(".pagination-nav.centre li.active span")).getText());
	}

	public boolean isBackToFirstPageButtonDisabled() {
		return  getParent(backToFirstPageButton()).getAttribute("class").contains("disabled");
	}

	public WebElement languageButton() {
		return findElement(By.cssSelector(".search-language .dropdown-toggle"));
	}

	public int countSearchResults() {
        ((JavascriptExecutor) getDriver()).executeScript("scroll(0,0)");
		final String bracketedSearchResultsTotal = new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".page-heading span"))).getText();
		return Integer.parseInt(bracketedSearchResultsTotal.substring(1, bracketedSearchResultsTotal.length() - 1));
	}

	public void emptyBucket() {
		for (final WebElement bucketItem : promotionsBucketWebElements()) {
			bucketItem.findElement(By.cssSelector(".remove-bucket-item")).click();
		}
	}

	public String getSelectedLanguage() {
		return findElement(By.cssSelector(".current-language-selection")).getText();
	}

	public WebElement getPromotionBucketElementByTitle(final String docTitle) {
        LoggerFactory.getLogger(SearchBase.class).info(docTitle);
        return findElement(By.cssSelector(".promotions-bucket-items")).findElement(By.xpath(".//*[contains(text(), '" + docTitle + "')]"));
	}

    /**
     * Takes a document title, and finds a document on the page with the same first five words
     * - this is to counteract the issue with Latin encoding where they cannot be found via xpath
     *
     * @param docTitle   Document title to attempt to find
     * @return           Web Element with the same first five words as the docTitle
     */
    public WebElement getPromotionBucketElementByTrimmedTitle(final String docTitle) {
        String trimmedTitle = "";
        int count = 0;

        //Get the first five words
        for(String word : docTitle.split(" ")){
            trimmedTitle = trimmedTitle + " " + word;

            if(count == 5){
                break;
            }

            count++;
        }

        //Find web element using trimmedTitle string. This will have a trailing space, and so you need to .trim() the String
        return findElement(By.cssSelector(".promotions-bucket-items")).findElement(By.xpath(".//*[contains(text(), '" + trimmedTitle.trim() + "')]"));
	}

	public WebElement getDatabasesList() {
		return findElement(By.cssSelector(".databases-list"));
	}

	public void selectDatabase(final String databaseName) {
		expandFilter(Filter.FILTER_BY);
		expandSubFilter(Filter.DATABASES);

		if (!getSelectedDatabases().contains(databaseName) ) {
			getParent(getDatabaseCheckboxes().get(getAllDatabases().indexOf(databaseName))).click();
			loadOrFadeWait();
		}
	}

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

	public void selectAllIndexes() {
		expandFilter(Filter.FILTER_BY);
		expandSubFilter(Filter.INDEXES);
		if (!findElement(By.xpath(".//label[text()[contains(., 'All')]]/div")).getAttribute("class").contains("checked")) {
			findElement(By.xpath(".//label[text()[contains(., 'All')]]/div/ins")).click();
			waitForSearchLoadIndicatorToDisappear();
		}
	}

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

	public List<String> getSelectedDatabases() {
		final List<String> selected = new ArrayList<>();

		for (final WebElement tick : getDatabasesList().findElements(By.cssSelector(".child-categories .checked"))) {
			selected.add(getParent(tick).getText());
		}

		return selected;
	}

	public WebElement leadSynonym(final String synonym) {
		return findElement(By.cssSelector(".search-synonyms-keywords")).findElement(By.xpath(".//ul[contains(@class, 'keywords-sub-list')]/li[1][@data-term='" + synonym + "']"));
	}

	public int countSynonymLists() {
		//return findElements(By.cssSelector(".search-synonyms-keywords .keywords-sub-list .btn-default")).size();
        // TODO possibly change this back
		//return findElements(By.className("add-synonym")).size();
		return (findElement(By.className("search-synonyms-keywords"))).findElements(By.className("add-synonym")).size();
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
		return findElement(By.cssSelector(".current-field-text-container")).findElement(By.xpath(".//button[contains(text(), 'Edit')]"));
	}

	public WebElement fieldTextRemoveButton() {
		return findElement(By.cssSelector(".current-field-text-container")).findElement(By.xpath(".//button[contains(text(), 'Remove')]"));
	}

	public void clearFieldText() {
		if (!fieldTextAddButton().isDisplayed()) {
			getDatabasesList().click();
			fieldTextRemoveButton().click();
		}
	}

	public int visibleDocumentsCount() {
		return findElements(By.cssSelector(".fa-file-o")).size();
	}

	public void showFieldTextOptions() {
		expandFilter(Filter.FIELD_TEXT);
	}

	public void collapseFieldTextOptions() {
		collapseFilter("Field Text");
	}

	public WebElement getFilter(final String filter) {
		return findElement(By.xpath(".//h4[contains(text(), '" + filter + "')]/.."));
	}

	public WebElement getSubFilter(final Filter filter) {
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
			getSubFilter(filterName).click();
			loadOrFadeWait();
		}
	}

	public void collapseFilter(final String filterName) {
		if (!getFilter(filterName).getAttribute("class").contains("collapsed")) {
			getFilter(filterName).click();
			loadOrFadeWait();
		}
	}

	public void showRelatedConcepts() {
		expandFilter(Filter.RELATED_CONCEPTS);
	}

	public WebElement waitForDocLogo() {
		return new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".fa-file-o")));
	}

	public void waitForSearchLoadIndicatorToDisappear() {
		int count = 0;
		try {
			while (findElement(By.cssSelector(".search-information h3")).getText().contains("Loading...") && count < 50){
//				System.out.println("Loading");
				loadOrFadeWait();
				count++;
			}
		} catch (final StaleElementReferenceException | org.openqa.selenium.NoSuchElementException e) {
			System.out.println("No Loading");
		}
	}

    public void waitForPromotionsLoadIndicatorToDisappear() {
        new WebDriverWait(getDriver(),60).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@data-cbox-rel='promoted-items-title']")));
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

	protected List<String> bucketList(final WebElement element) {
		final List<String> bucketDocTitles = new ArrayList<>();
		for (final WebElement bucketDoc : element.findElements(By.cssSelector(".promotions-bucket-document"))) {
			bucketDocTitles.add(bucketDoc.getText());
		}
		return bucketDocTitles;
	}

	public void openFromDatePicker() {
		findElement(By.cssSelector("[data-filter-name=\"minDate\"] .clickable")).click();
		loadOrFadeWait();
	}

	public void closeFromDatePicker() {
		if (getDriver().findElements(By.cssSelector(".datepicker")).size() > 0) {
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
		if (getDriver().findElements(By.cssSelector(".datepicker")).size() > 0) {
			findElement(By.cssSelector("[data-filter-name=\"maxDate\"] .clickable")).click();
			loadOrFadeWait();
			waitForSearchLoadIndicatorToDisappear();
		}
	}

	public Date getDateFromResult(final int index) throws ParseException {
		final String dateString = getParent(getSearchResult(index)).findElement(By.cssSelector(".date")).getText();
		if (dateString.equals("")) {
			return null;
		}
		return DATE_FORMAT.parse(dateString.split(", ")[1]);
	}

	public String getResultsForText() {
		return getDriver().findElement(By.cssSelector(".heading strong")).getText();
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

	public WebElement parametricValueLoadIndicator() {
		return findElement(By.cssSelector(".search-parametric .processing-indicator"));
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

	public boolean isErrorMessageShowing() {
		return !findElement(By.cssSelector(".search-information")).getAttribute("class").contains("hide");
	}

	public String getTopPromotedLinkTitle() {
		return findElement(By.cssSelector(".promotions .search-result-title")).getText();
	}

	public String getTopPromotedLinkButtonText() {
		return findElement(By.cssSelector(".search-result .promotion-name")).getText();
	}

	public void sortByRelevance() {
		sortBy("by relevance");
	}

	public void sortByDate() {
		sortBy("by date");
	}

	public void sortBy(final String sortBy) {
		findElement(By.cssSelector(".current-search-sort")).click();

		final WebElement element = findElement(By.cssSelector(".search-results-sort")).findElement(By.xpath(".//a[text()='" + sortBy + "']"));
		// IE doesn't like clicking dropdown elements
		final JavascriptExecutor executor = (JavascriptExecutor)getDriver();
		executor.executeScript("arguments[0].click();", element);

		loadOrFadeWait();
		waitForSearchLoadIndicatorToDisappear();
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

	public Date getDateFromFilter(final WebElement filter) throws ParseException {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return dateFormat.parse(filter.getAttribute("value"));
	}

	public void sendDateToFilter(final Date date, final WebElement filter) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		filter.clear();
		filter.sendKeys(dateFormat.format(date));
	}

	public List<String> filterLabelList() {
		return webElementListToStringList(findElements(By.cssSelector(".filter-display-view .filter-display-text")));
	}

	public List<String> getLeadSynonymsList() {
		final List<String> leadSynonyms = new ArrayList<>();
		for (final WebElement synonymGroup : findElements(By.cssSelector(".keywords-list > ul > li"))) {
			leadSynonyms.add(synonymGroup.findElement(By.cssSelector("li:first-child span span")).getText());
		}
		return leadSynonyms;
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
}
