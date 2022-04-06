/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

package com.autonomy.abc.selenium.find.results;

import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.query.QueryResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class FindResult extends QueryResult {
    FindResult(final WebElement result, final WebDriver driver) {
        super(result, driver);
    }

    @Override
    public WebElement title() {
        return findElement(By.xpath(".//*[@class[contains(.,'result-header')] and text()[normalize-space()]]"));
    }

    public String link() {
        return findElement(By.cssSelector("a")).getAttribute("href");
    }

    @Override
    public WebElement icon() {
        return findElement(By.cssSelector(".content-type i"));
    }

    public String getReference() {
        return findElement(By.className("document-reference")).getText();
    }

    public String getDate() {
        return findElement(By.className("document-date")).getText();
    }

    public WebElement similarDocuments() {
        return findElement(By.className("similar-documents-trigger"));
    }

    private WebElement previewButton() {
        return findElement(By.className("preview-link"));
    }

    private Boolean previewButtonExists() {
        return !findElements(By.className("preview-link")).isEmpty();
    }

    public InlinePreview openDocumentPreview() {
        if (previewButtonExists()) {
            previewButton().click();
        } else {
            title().click();
        }

        return InlinePreview.make(getDriver());
    }

    public ZonedDateTime convertRelativeDate(final ZonedDateTime timeNow) {
        final String vagueDate = getDate();
        final String[] words = vagueDate.split(" ");

        final int timeAmount;
        final String timeUnit;
        if ("a".equals(words[0]) || "an".equals(words[0])) {
            // e.g. a year ago
            timeAmount = -1;
            timeUnit = words[1];
        } else if ("in".equals(words[0])) {
            // e.g. in 6 months
            timeAmount = "a".equals(words[1]) || "an".equals(words[1]) ? -1 : Integer.parseInt(words[1]);
            timeUnit = words[2];
        } else {
            // e.g. 2 months ago
            timeAmount = -Integer.parseInt(words[0]);
            timeUnit = words[1];
        }

        return parseRelativeTime(timeNow, timeAmount, timeUnit);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private ZonedDateTime parseRelativeTime(final ZonedDateTime timeNow, final int timeAmount, final String timeUnit) {
        TemporalUnit temporalUnit = ChronoUnit.SECONDS;
        switch (timeUnit) {
            case "minute":
            case "minutes":
                temporalUnit = ChronoUnit.MINUTES;
                break;

            case "hour":
            case "hours":
                temporalUnit = ChronoUnit.HOURS;
                break;

            case "day":
            case "days":
                temporalUnit = ChronoUnit.DAYS;
                break;

            case "month":
            case "months":
                temporalUnit = ChronoUnit.MONTHS;
                break;

            case "year":
            case "years":
                temporalUnit = ChronoUnit.YEARS;
                break;
        }

        return timeNow.plus(timeAmount, temporalUnit);
    }
}
