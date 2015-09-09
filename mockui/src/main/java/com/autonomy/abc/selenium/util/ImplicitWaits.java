/*
Sometimes it may be useful to temporarily deactivate the implicit wait (cf.
 http://stackoverflow.com/questions/11034710/temporarily-bypassing-implicit-waits-with-webdriver )
 */

package com.autonomy.abc.selenium.util;

import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class ImplicitWaits {

	private ImplicitWaits() {
		throw new AssertionError(ImplicitWaits.class.getCanonicalName() + " cannot be instantiated");
	}

	public static void setImplicitWait(final WebDriver driver) {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	public static void removeImplicitWait(final WebDriver driver) {
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.MICROSECONDS);
	}

}
