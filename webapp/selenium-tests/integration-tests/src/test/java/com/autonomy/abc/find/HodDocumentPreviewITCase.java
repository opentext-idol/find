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

import com.autonomy.abc.base.HodFindTestBase;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.results.DocumentViewer;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.openqa.selenium.lift.Matchers.displayed;

public class HodDocumentPreviewITCase extends HodFindTestBase {
    private static final Pattern PROTOCOL_SUFFIX = Pattern.compile("://");

    private FindPage findPage;
    private FindService findService;

    public HodDocumentPreviewITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }

    @Test
    @ResolvedBug("CSA-1767 - footer not hidden properly")
    public void testViewDocumentsOpenFromFind() {
        final ListView results = findService.search("Review");

        for(final FindResult result : results.getResults(5)) {
            final DocumentViewer docViewer = result.openDocumentPreview();
            verifyDocumentViewer(docViewer);
            docViewer.close();
        }
    }

    private void verifyDocumentViewer(final DocumentViewer docViewer) {
        final Frame frame = new Frame(getDriver(), docViewer.frame());

        verifyThat("document visible", docViewer, displayed());

        frame.operateOnContent(content -> {
            final Locator errorHeader = new Locator()
                    .withTagName("h1")
                    .containingText("500");
            final Locator errorBody = new Locator()
                    .withTagName("h2")
                    .containingCaseInsensitive("error");
            verifyThat("no backend error", content.findElements(errorHeader), empty());
            verifyThat("no view server error", content.findElements(errorBody), empty());
            return null;
        });
    }

    @Test
    @ResolvedBug("CCUK-3647")
    //TODO possibility that scrolling isn't working on vm
    public void testMessageWhenRunOutOfResults() {
        final ListView results = findService.search(new Query("connectors"));
        getElementFactory().getFilterPanel().indexesTreeContainer().expand();
        findPage.filterBy(new IndexFilter("Site Search"));

        int count = 0;
        while(count < 10 && !findPage.resultsMessagePresent()) {
            findPage.scrollToBottom();
            count++;
        }

        verifyThat(results.resultsDiv(), containsText("No more results found"));
    }

    @Test
    @ResolvedBug("CSA-1767 - footer not hidden properly")
    @RelatedTo({"CSA-946", "CSA-1656", "CSA-1657", "CSA-1908"})
    public void testDocumentPreview() {
        final Index index = new Index("fifa");
        final ListView results = findService.search(new Query("document preview"));
        getElementFactory().getFilterPanel().indexesTreeContainer().expand();
        findPage.filterBy(new IndexFilter(index));

        for(final FindResult queryResult : results.getResults(5)) {
            final DocumentViewer documentViewer = queryResult.openDocumentPreview();
            checkDocumentPreview(documentViewer, index);
            documentViewer.close();
        }
    }

    private void checkDocumentPreview(final DocumentViewer documentViewer, final Index index) {
        verifyThat(documentViewer.getIndex(), is(index));
        verifyThat("Reference is displayed", documentViewer.getReference(), CoreMatchers.not(isEmptyOrNullString()));

        final String frameText = new Frame(getDriver(), documentViewer.frame()).getText();

        verifyThat("Frame has content", frameText, CoreMatchers.not(isEmptyOrNullString()));
        verifyThat(frameText, CoreMatchers.not(CoreMatchers.containsString("server error")));
    }

    @Test
    @ResolvedBug("FIND-497")
    public void testOpenDocumentFromSearch() {
        final ListView results = findService.search("Window");
        final String mainWindowHandle = getDriver().getWindowHandle();

        for(int i = 1; i <= 5; i++) {
            final FindResult result = results.getResult(i);
            final String reference = result.getReference();
            result.title().click();
            assertThat("Link does not contain 'undefined'", result.link(), not(containsString("undefined")));
            final String newWindowHandle = getDriver().getWindowHandles().stream().filter(s -> !s.equals(mainWindowHandle)).findFirst().get();
            getDriver().switchTo().window(newWindowHandle);
            verifyThat(getDriver().getCurrentUrl(), containsString(PROTOCOL_SUFFIX.split(reference)[1]));
            getDriver().close();
            getDriver().switchTo().window(mainWindowHandle);
        }
    }
}
