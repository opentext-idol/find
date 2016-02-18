package com.autonomy.abc.selenium.analytics;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class DashboardBase extends AppElement implements AppPage {
    public DashboardBase(WebElement element, WebDriver driver) {
        super(element, driver);
    }
}
