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

package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.preview.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ListView;
import com.hp.autonomy.frontend.selenium.config.Browser;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.openqa.selenium.lift.Matchers.displayed;

public class DocumentPreviewITCase extends FindTestBase {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(" ", Pattern.LITERAL);
    private static final Pattern PROTOCOL_SUFFIX = Pattern.compile("://");
    private FindPage findPage;
    private FindService findService;

    public DocumentPreviewITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        findPage.goToListView();
    }

    @Test
    public void testShowDocumentPreview() {
        final ListView results = findService.search("cake");
        final InlinePreview docPreview = results.searchResult(1).openDocumentPreview();

        if (docPreview.loadingIndicatorExists()) {
            assertThat("Preview not stuck loading", docPreview.loadingIndicator(), not(displayed()));
        }

        assertThat("Index displayed", docPreview.getIndexName(), not(isEmptyOrNullString()));
        assertThat("Reference displayed", docPreview.getReference(), not(isEmptyOrNullString()));

        final Frame previewFrame = new Frame(getDriver(), docPreview.frame());
        final String frameText = previewFrame.getText();

        verifyThat("Preview document has content", previewFrame.operateOnContent(WebElement::getTagName), is("body"));
        assertThat("Preview document has no error", frameText, not(containsString("encountered an error")));

        docPreview.close();
    }

    @Test
    @ResolvedBug("FIND-497")
    //WARNING: may fail for some data if indexed URL now redirects to a newer version of the wiki page.
    public void testOpenOriginalDocInNewTab() {
        final ListView results = findService.search("general");
        results.waitForResultsToLoad();

        for (final FindResult queryResult : results.getResults(4)) {
            final InlinePreview docViewer = queryResult.openDocumentPreview();

            final String reference = docViewer.getReference();
            final DetailedPreviewPage detailedPreviewPage = docViewer.openPreview();

            assertThat("Link does not contain 'undefined'", detailedPreviewPage.originalDocLink(), not(containsString("undefined")));
            assertThat("Page not blank", detailedPreviewPage.frameExists());

            switchToNewWindow(detailedPreviewPage::openOriginalDoc);
            final String decodedURL = decodeURL(getDriver().getCurrentUrl());
            verifyThat(decodedURL, containsString(reformatReference(reference)));
            getDriver().close();
            getMainWindow().activate();
            detailedPreviewPage.goBackToSearch();
        }
    }

    private String decodeURL(final String encoded) {
        try {
            return URLDecoder.decode(encoded, "UTF8");
        } catch (final UnsupportedEncodingException e) {
            LOGGER.info("Could not decode the URL", e);
            return encoded;
        }
    }

    private Serializable reformatReference(final CharSequence badFormatReference) {
        return PROTOCOL_SUFFIX.split(WHITESPACE_PATTERN.matcher(badFormatReference).replaceAll(Matcher.quoteReplacement("_")))[1].split("/")[0];
    }

    @Test
    public void testDetailedPreview() {
        final ListView results = findService.search("face");

        final InlinePreview inlinePreview = results.getResult(1).openDocumentPreview();
        final DetailedPreviewPage detailedPreviewPage = inlinePreview.openPreview();

        final String frameText = new Frame(getDriver(), detailedPreviewPage.frame()).getText();
        verifyThat("Frame has content", frameText, not(isEmptyOrNullString()));
        verifyThat("Preview frame has no error", frameText, not(containsString("encountered an error")));

        checkHasMetaDataFields(detailedPreviewPage);
        checkSimilarDocuments(detailedPreviewPage);

        if (Objects.equals(getApplication().getClass(), BIIdolFind.class)) {
            checkSimilarDates(detailedPreviewPage);
        }

        detailedPreviewPage.goBackToSearch();
    }

    private void checkHasMetaDataFields(final DetailedPreviewPage detailedPreviewPage) {
        verifyThat("Detailed Preview has reference", detailedPreviewPage.getReference(), not(nullValue()));

        if (isHosted()) {
            verifyThat("Detailed Preview has index", detailedPreviewPage.getIndex(), not(nullValue()));
        } else {
            verifyThat("Detailed Preview has database", detailedPreviewPage.getDatabase(), not(nullValue()));
        }

        verifyThat("Detailed Preview has title", detailedPreviewPage.getTitle(), not(nullValue()));
        verifyThat("Detailed Preview has summary", detailedPreviewPage.getSummary(), not(nullValue()));
        verifyThat("Detailed Preview has date", detailedPreviewPage.getDate(), not(nullValue()));
    }

    private void checkSimilarDocuments(final DetailedPreviewPage detailedPreviewPage) {
        detailedPreviewPage.similarDocsTab().click();
        detailedPreviewPage.waitForTabToLoad();
        verifyThat("Tab loads", !detailedPreviewPage.tabLoadingIndicator().isDisplayed());
    }

    private void checkSimilarDates(final DetailedPreviewPage detailedPreviewPage) {
        detailedPreviewPage.similarDatesTab().click();
        detailedPreviewPage.waitForTabToLoad();
        verifyThat("Tab loads", !detailedPreviewPage.tabLoadingIndicator().isDisplayed());
        changeDateSliderToYearBefore(detailedPreviewPage);
        verifyThat("Can change to similar docs from year before", detailedPreviewPage.getSimilarDatesSummary(), containsString("Between 1 year"));
    }

    private void changeDateSliderToYearBefore(final DetailedPreviewPage detailedPreviewPage) {
        detailedPreviewPage.ithTick(1).click();
    }

    @Test
    @ActiveBug(value = "FIND-86", browsers = Browser.FIREFOX)
    public void testOneCopyOfDocInDetailedPreview() {
        final ListView results = findService.search("face");
        final InlinePreview inlinePreview = results.getResult(1).openDocumentPreview();

        final DetailedPreviewPage detailedPreviewPage = inlinePreview.openPreview();

        verifyThat("Only 1 copy of that document in detailed preview", detailedPreviewPage.numberOfHeadersWithDocTitle(), lessThanOrEqualTo(1));

        detailedPreviewPage.goBackToSearch();
    }

    @Test
    @ResolvedBug("FIND-672")
    public void testPreviewFillsFrame() {
        final ListView results = findService.search("face");

        final InlinePreview inlinePreview = results.getResult(1).openDocumentPreview();
        assertThat("iframe containing document not squashed", inlinePreview.docFillsMoreThanHalfOfPreview());
    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
