package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class EditDocumentReferencesPage extends SearchBase implements AppPage {

	public EditDocumentReferencesPage(final TopNavBar topNavBar, final WebElement element) {
		super(element, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage() {
		getDriver().get("/promotions/edit");
	}

	public WebElement saveButton() {
		return findElement(By.cssSelector(".edit-document-references")).findElement(By.xpath(".//*[text() = 'Save']"));
	}

	public List<String> promotionsBucketList() {
		return bucketList(findElement(By.cssSelector(".edit-document-references")));
	}

	public List<WebElement> promotionsBucketWebElements() {
		return findElement(By.cssSelector(".edit-document-references")).findElements(By.xpath(".//*[contains(@class, 'promotions-bucket-document')]/.."));
	}

	public WebElement cancelButton() {
		return findElement(By.xpath(".//a[contains(text(), 'Cancel')]"));
	}

	public static class Placeholder {

		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public EditDocumentReferencesPage $editReferences(final WebElement element) {
			return new EditDocumentReferencesPage(topNavBar, element);
		}
	}
}
