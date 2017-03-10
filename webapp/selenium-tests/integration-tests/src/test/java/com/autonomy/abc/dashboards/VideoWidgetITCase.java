/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

public class VideoWidgetITCase extends ClickableDashboardITCase {
    public VideoWidgetITCase(final TestConfig config) {
        super(config, 1, "Video Dashboard", "List", "VideoSearch");
    }

    @Test
    public void testVideoElementExists() { // not sure if this test is any use
        final WebElement webElement = page.getWidgets().get(0);
        final WebElement videoElement = webElement.findElement(By.cssSelector(".video-container video"));
        assertThat("class has not been rendered", videoElement != null);
    }

    @Test
    public void testVideoElementAttributes() {
        final WebElement webElement = page.getWidgets().get(0);
        final WebElement videoElement = webElement.findElement(By.cssSelector(".video-container video"));
        assertThat("video is not looping", Boolean.valueOf(videoElement.getAttribute("loop")));
        assertThat("video is not muted", Boolean.valueOf(videoElement.getAttribute("muted")));
    }
}
