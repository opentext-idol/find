package com.autonomy.abc.selenium.page.search;

import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
