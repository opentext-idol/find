package com.autonomy.abc.selenium.analytics;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class DashboardBase extends AppElement implements AppPage {
    // this is an abstract class rather than an interface because
    // PageMapper does not currently support interfaces
    public DashboardBase(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }
}
