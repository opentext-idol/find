package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public class StaticImageWidgetITCase extends DashboardITCase {

    public StaticImageWidgetITCase(final TestConfig config) {
        super(config, 1, "Static Image Dashboard");
    }

    @Test
    public void testImageElementExists() { // not sure if this test is any use
        final WebElement webElement = page.getWidgets().get(0);
        final WebElement staticImageElement = webElement.findElement(By.cssSelector(".static-image"));
        assertThat("class has not been rendered", staticImageElement != null);
    }

    @Test
    public void testImageElementSrc() {
        final WebElement webElement = page.getWidgets().get(0);
        final WebElement staticImageElement = webElement.findElement(By.cssSelector(".static-image"));
        final String src = staticImageElement.getCssValue("background-image");
        assertThat("src is incorrect", "url(\"http://placehold.it/800x300\")".equals(src));
    }
}
