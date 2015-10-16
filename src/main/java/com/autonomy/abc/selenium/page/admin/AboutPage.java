package com.autonomy.abc.selenium.page.admin;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AboutPage extends AppElement implements AppPage {

    private AboutPage(final WebDriver driver) {
        super(driver.findElement(By.cssSelector(".wrapper-content")), driver);
    }

    public static AboutPage make(final WebDriver driver) {
        AboutPage.waitForLoad(driver);
        return new AboutPage(driver);
    }

	public void setTableSize(final String tableSize) {
		findElement(By.cssSelector("[name='DataTables_Table_0_length'] option[value='" + tableSize + "']")).click();
	}

	public WebElement nextButton() {
		return findElement(By.cssSelector(".next a"));
	}

	public WebElement previousButton() {
		return findElement(By.cssSelector(".previous a"));
	}

	public boolean isPreviousDisabled() {
		return findElement(By.cssSelector(".previous")).getAttribute("class").contains("disabled");
	}

	public boolean isNextDisabled() {
		return findElement(By.cssSelector(".next")).getAttribute("class").contains("disabled");
	}

	public boolean isPageinateNumberActive(final int pageinateNumber) {
		return findElement(By.xpath(".//li[contains(@class, 'paginate_button active')]/a")).getText().equals(String.valueOf(pageinateNumber));
	}

	public WebElement pageinateNumber(final int pageinateNumber) {
		return findElement(By.xpath(".//a[text()='" + String.valueOf(pageinateNumber) + "']"));
	}

	public void searchInSearchBox(final String searchTerm) {
		findElement(By.cssSelector(".dataTables_filter [type='search']")).clear();
		findElement(By.cssSelector(".dataTables_filter [type='search']")).sendKeys(searchTerm);
	}

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    public static void waitForLoad(final WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//h5[text()='FOSS Acknowledgements']")));
    }

}
