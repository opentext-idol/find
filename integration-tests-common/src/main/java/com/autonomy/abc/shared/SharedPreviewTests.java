package com.autonomy.abc.shared;

import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.search.SearchResult;
import com.hp.autonomy.frontend.selenium.util.Waits;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

public class SharedPreviewTests {

    public static void testDocumentPreviews(Session session, List<? extends SearchResult> searchResults, Index index) {
        for(SearchResult searchResult : searchResults) {
            DocumentViewer documentViewer = searchResult.openDocumentPreview();
            testDocumentPreview(session, documentViewer, index);
            documentViewer.close();
        }
    }

    public static void testDocumentPreview(Session session, DocumentViewer documentViewer, Index index){
        if(index == null){
            verifyThat("index is displayed", documentViewer.getIndex(), not(nullValue()));
        } else {
            verifyThat(documentViewer.getIndex(), is(index));
        }

        verifyThat("reference is displayed", documentViewer.getReference(), not(isEmptyOrNullString()));
        //this isn't being used on HSOD; CSA-1986 needs to be completed
        verifyThat("content type is displayed", documentViewer.getContentType(), not(isEmptyOrNullString()));

        String frameText = new Frame(session.getActiveWindow(), documentViewer.frame()).getText();

        verifyThat("frame has content", frameText, not(isEmptyOrNullString()));
        verifyThat(frameText, not(containsString("server error")));

        testOpenInNewTabFromViewer(session, documentViewer);
    }

    public static void testDocumentPreview(Session session, DocumentViewer documentViewer){
        testDocumentPreview(session, documentViewer, null);
    }

    public static void testOpenInNewTabFromViewer(Session session, DocumentViewer documentViewer){
        String reference = documentViewer.getReference();

        Window original = session.getActiveWindow();
        documentViewer.openInNewTab();
        Waits.loadOrFadeWait();
        Window newWindow = session.switchWindow(session.countWindows() - 1);
        newWindow.activate();

        verifyThat(session.getDriver().getCurrentUrl(), is(reference));

        newWindow.close();
        original.activate();
    }
}
