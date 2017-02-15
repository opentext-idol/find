package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public class TopicMapWidgetITCase extends ClickableDashboardITCase {

    public TopicMapWidgetITCase(final TestConfig config) {
        super(config, 1, "TopicMap Dashboard", "Topic Map", "ListSearch");
    }

    //this needs more robust testing when possible

    @Test
    public void testElementExists() {
        final WebElement webElement = page.getWidgets().get(0);
        assertThat("class has not been rendered", webElement.findElement(By.cssSelector(".entity-topic-map")) != null);
        assertThat("svg has not rendered", webElement.findElement(By.cssSelector(".entity-topic-map svg")) != null);
    }
}
