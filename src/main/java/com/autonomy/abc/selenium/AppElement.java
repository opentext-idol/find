package com.autonomy.abc.selenium;

import com.autonomy.abc.selenium.util.AbstractWebElementPlaceholder;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class AppElement implements WebElement {

	private final WebElement $el;
	private final WebDriver driver;

	public AppElement(final WebElement element, final WebDriver driver) {
		this.$el = element;
		this.driver = driver;
	}

	public WebElement $el() {
		return $el;
	}

	public WebDriver getDriver() {
		return driver;
	}

	@Override
	public WebElement findElement(final By by) {
		return $el().findElement(by);
	}

	@Override
	public List<WebElement> findElements(final By by) {
		return $el().findElements(by);
	}

	@Override
	public boolean isDisplayed() {
		return $el().isDisplayed();
	}

	@Override
	public Point getLocation() {
		return $el().getLocation();
	}

	@Override
	public Dimension getSize() {
		return $el().getSize();
	}

	@Override
	public String getCssValue(final String propertyName) {
		return $el().getCssValue(propertyName);
	}

	@Override
	public void click() {
		$el().click();
	}

	@Override
	public void submit() {
		$el().submit();
	}

	@Override
	public void sendKeys(final CharSequence... keysToSend) {
		$el().sendKeys(keysToSend);
	}

	@Override
	public void clear() {
		$el().clear();
	}

	@Override
	public String getTagName() {
		return $el().getTagName();
	}

	@Override
	public String getAttribute(final String name) {
		return $el().getAttribute(name);
	}

	@Override
	public boolean isSelected() {
		return $el().isSelected();
	}

	@Override
	public boolean isEnabled() {
		return $el().isEnabled();
	}

	@Override
	public String getText() {
		return $el().getText();
	}

	public static WebElement getParent(final WebElement child) {
		return child.findElement(By.xpath(".//.."));
	}

	public boolean hasClass(final String className) {
		return hasClass(className, $el());
	}

	public static boolean hasClass(final String className, final WebElement element) {
		final Set<String> classes = getClassSet(element);

		return classes.contains(className);
	}

	public static Set<String> getClassSet(final WebElement element) {
		final Set<String> output = new HashSet<>();
		final String classAttribute = element.getAttribute("class");

		if (classAttribute != null && !classAttribute.isEmpty()) {
			output.addAll(Arrays.asList(classAttribute.split(" +")));
		}

		return output;
	}

	public void loadOrFadeWait() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {/*NOOP*/}
	}

	public void waitUntilClickableThenClick(final By by) {
		final WebDriverWait waiting = new WebDriverWait(getDriver(),10);
		waiting.until(ExpectedConditions.visibilityOfElementLocated(by));
		findElement(by).click();
	}

	public void waitUntilClickableThenClick(final WebElement element) {
		final WebDriverWait waiting = new WebDriverWait(getDriver(),10);
		waiting.until(ExpectedConditions.visibilityOf(element));
		element.click();
	}

	// try catch added because in Chrome a span covers the button when the button is disabled, causing it to be clicked instead which throws WebDriverException
	public void tryClickThenTryParentClick(final WebElement element) {
		try {
			element.click();
		} catch (final WebDriverException e) {
			getParent(element).click();
		}
	}

	public List<String> loadTextFileLineByLineIntoList(final String filePath) throws IOException {
		final FileInputStream fis = new FileInputStream(filePath);

		try (final BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String line;
			final List<String> fiveWords = new ArrayList<>();

			while ((line = br.readLine()) != null) {
				fiveWords.add(line);
			}

			return fiveWords;
		}
	}

	// TODO: Escape double quotes
	public String cleanXpathString(final String dirtyString) {
		final String cleanString;
		if (dirtyString.contains("'")) {
			cleanString = "concat(\"" + dirtyString.replace("'", "\", \"'\", \"") + "\")";
		} else {
			cleanString = "'" + dirtyString + "'";
		}
		return cleanString;
	}

	public boolean isAttributePresent(final WebElement element, final String attribute) {
		boolean result = false;
		try {
			if (element.getAttribute(attribute) != null) {
				result = true;
			}
		} catch (final Exception e) {}

		return result;
	}

	public void waitForGritterToClear() throws InterruptedException {
		Thread.sleep(3008);
	}

	@Override
	public String toString() {
		return $el == null ? "element is stale" : $el().getTagName() + " : " + $el().toString();
	}

	@Override
	public boolean equals(final Object that) {
		if (!(that instanceof AppElement)) {
			return false;
		}

		final AppElement elem = (AppElement) that;

		return this.$el().equals(elem.$el());
	}

	@Override
	public int hashCode() {
		return this.$el().hashCode();
	}

	public String title() {
		return findElement(By.cssSelector(".page-title")).getText();
	}

	public String getLastWord(final String string) {
		return string.substring(string.lastIndexOf(" ") + 1);
	}

	/**
	 * Create a new browser window. Returns both the current window handle and the handle for the newly created window.
	 * @return Window handles. The first item is the old handle, and the second is the new one.
	 */
	public List<String> createAndListWindowHandles() {
		final Set<String> windows = getDriver().getWindowHandles();
		final String handle = getDriver().getWindowHandle();
		final JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		executor.executeScript("window.open('hello', '', 'width=1000');");

		final Set<String> moreWindows = getDriver().getWindowHandles();
		moreWindows.removeAll(windows);
		final String secondHandle = ((String)moreWindows.toArray()[0]);

		return Arrays.asList(handle, secondHandle);
	}

	public boolean isAlertPresent()
	{
		try {
			getDriver().switchTo().alert();
			return true;
		} catch (NoAlertPresentException Ex) {
			return false;
		}
	}

	public WebElement getFirstChild(final WebElement parent) {
		return parent.findElement(By.xpath(".//*"));
	}

	public void sendBackspaceToWebElement(final WebElement element, final int numberOfBackspaces) {
		for (int i = 0; i < numberOfBackspaces; i++) {
			element.sendKeys(Keys.BACK_SPACE);
		}
	}

	public static class Placeholder extends AbstractWebElementPlaceholder<AppElement> {

		public Placeholder(final AppElement ancestor, final By by) {
			super(ancestor, by);
		}


		@Override
		protected AppElement convertToActualType(final WebElement element) {
			return new AppElement(element, getDriver());
		}
	}

	// scroll methods: currently only do vertical scroll.
	public void scrollIntoView() {
		scrollIntoView(this, getDriver());
	}

	public static void scrollIntoView(final WebElement element, final WebDriver driver) {
		final JavascriptExecutor executor = (JavascriptExecutor) driver;
		final int centre = element.getLocation().getY() + element.getSize().height / 2;
		executor.executeScript("window.scrollTo(0, " + centre + " - Math.floor(window.innerHeight/2));");
	}
}
