/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class CurrentTimeWidgetITCase extends DashboardITCase {
    public CurrentTimeWidgetITCase(final TestConfig config) {
        super(config, 1, "Current Time Date Dashboard");
    }

    @Test
    public void testTimeFormat() {
        final WebElement webElement = page.getWidgets().get(0);
        final String currentTime = webElement.findElement(By.cssSelector(".current-time")).getText();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm z");
        dateTimeFormatter.parse(currentTime);
    }

    @Test
    public void testDayFormat() {
        final WebElement webElement = page.getWidgets().get(0);
        final String currentDay = webElement.findElement(By.cssSelector(".day")).getText();

        final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("EEEE")
                .toFormatter();

        dateTimeFormatter.parse(currentDay);
    }

    @Test
    public void testDateFormat() {
        final WebElement webElement = page.getWidgets().get(0);
        final String currentDate = webElement.findElement(By.cssSelector(".date")).getText();

        final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM dd, uuuu")
                .toFormatter();

        dateTimeFormatter.parse(currentDate);
    }
}
