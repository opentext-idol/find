package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchBase extends AppElement implements AppPage {

	public SearchBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public WebElement searchResultCheckbox(final int resultNumber) {
		return findElement(By.cssSelector(".search-results li:nth-child(" + String.valueOf(resultNumber) + ") .icheckbox_square-blue"));
	}

	public int promotedItemsCount() {
		return findElements(By.cssSelector(".promoted-items .fa")).size();
	}

	public List<String> promotionsBucketList() {
		final List<String> bucketDocTitles = new ArrayList<>();
		for (final WebElement bucketDoc : findElements(By.cssSelector(".promotions-bucket-document"))) {
			bucketDocTitles.add(bucketDoc.getText());
		}
		return bucketDocTitles;
	}

}
