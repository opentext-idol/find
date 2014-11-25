package com.autonomy.abc.selenium.page;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.WebElement;

public class KeywordsPage extends AppElement implements AppPage {

	public KeywordsPage(final SideNavBar sideNavBar, final WebElement $el) {
		super($el, sideNavBar.getDriver());
	}

	@Override
	public void navigateToPage() {
		getDriver().get("promotions");
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<KeywordsPage> {

		public Placeholder(final AppBody body, final SideNavBar sideNavBar, final TopNavBar topNavBar) {
			super(body, sideNavBar, topNavBar, "keywords", "keywords", false);
		}

		@Override
		protected KeywordsPage convertToActualType(final WebElement element) {
			return new KeywordsPage(navBar, element);
		}

	}
}
