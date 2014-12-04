package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class EditDocumentReferencesPage extends AppElement implements AppPage {

	public EditDocumentReferencesPage(final TopNavBar topNavBar, final WebElement element) {
		super(element, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage() {
		getDriver().get("/promotions/edit");
	}

	public WebElement saveButton() {
		return findElement(By.cssSelector(".edit-document-references ")).findElement(By.xpath(".//*[text() = 'Save']"));
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
