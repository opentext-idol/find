/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrentTimeWidgetITCase extends DashboardITCase {
    private static final Pattern AM = Pattern.compile("am", Pattern.LITERAL);
    private static final Pattern PM = Pattern.compile("pm", Pattern.LITERAL);

    public CurrentTimeWidgetITCase(final TestConfig config) {

        super(config, 1, "Current Time Date Dashboard");
    }

    @Test
    public void testTimeFormat() {
        final WebElement webElement = page.getWidgets().get(0);
        final String currentTime = webElement.findElement(By.cssSelector(".current-time")).getText();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mma z");
        dateTimeFormatter.parse(PM.matcher(AM.matcher(currentTime).replaceAll(Matcher.quoteReplacement("AM"))).replaceAll(Matcher.quoteReplacement("PM")));
    }

    @Test
    public void testDayFormat() {
        final WebElement webElement = page.getWidgets().get(0);
        final String currentDay = webElement.findElement(By.cssSelector(".day")).getText();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE");
        dateTimeFormatter.parse(currentDay);
    }

    @Test
    public void testDateFormat() {
        final WebElement webElement = page.getWidgets().get(0);
        final String currentDate = webElement.findElement(By.cssSelector(".date")).getText();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, uuuu");
        dateTimeFormatter.parse(currentDate);
    }
}
